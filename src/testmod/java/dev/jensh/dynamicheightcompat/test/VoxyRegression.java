package dev.jensh.dynamicheightcompat.test;

import dev.jensh.dynamicheightcompat.VoxyCoordinates;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class VoxyRegression {
    private VoxyRegression() {}

    public static void checkCodec() {
        var keys = new HashSet<Long>();
        for (int y = -2048; y < 2048; y++) {
            long old = legacyKey(0, 864, y, 416);
            long key = VoxyCoordinates.extend(old, y);
            require(keys.add(key), "Vertical coordinates collided at " + y);
            require(VoxyCoordinates.y(key) == y, "CPU Y failed at " + y);
            int shaderLowY = ((int) (key >>> 32) << 4) >> 24;
            int shaderY = ((shaderLowY ^ (((int) key & 15) << 8)) << 20) >> 20;
            require(shaderY == y, "GPU layout Y failed at " + y);
            require((key & ~15L) == old, "X/Z/level encoding changed");
            if (y >= -128 && y <= 127) require(key == old, "Legacy key changed at " + y);
        }
        for (int level = 0; level <= 4; level++) {
            for (int y = -4; y <= 1023; y++) {
                int parentY = y >> level;
                long key = VoxyCoordinates.extend(legacyKey(level, 54, parentY, 26), parentY);
                long child = VoxyCoordinates.extend(legacyKey(0, 54 << level, parentY << level, 26 << level), parentY << level);
                require((VoxyCoordinates.y(key) << level) == VoxyCoordinates.y(child), "LOD hierarchy Y changed");
            }
        }
    }

    /** Uses the installed Voxy classes and the actual shader-loading path after Roxy transforms them. */
    public static void checkInstalled() throws Exception {
        checkCodec();
        var engine = Class.forName("me.cortex.voxy.common.world.WorldEngine");
        Method pack = engine.getMethod("getWorldSectionId", int.class, int.class, int.class, int.class);
        Method y = engine.getMethod("getY", long.class);
        var nodes = Class.forName("me.cortex.voxy.client.core.rendering.hierachical.NodeManager");
        Method assertValid = nodes.getDeclaredMethod("assertPosValid", long.class);
        assertValid.setAccessible(true);
        long crashPosition = (long) pack.invoke(null, 4, 54, 12, 26);
        assertValid.invoke(null, crashPosition);
        for (int sectionY : new int[]{-4, -1, 0, 127, 128, 192, 255, 256, 1023}) {
            long key = (long) pack.invoke(null, 0, 864, sectionY, 416);
            require((int) y.invoke(null, key) == sectionY, "Installed Voxy coordinate mismatch");
            assertValid.invoke(null, key);
        }
        var parser = Class.forName("me.cortex.voxy.client.core.gl.shader.ShaderLoader$ShaderLoadingParser");
        Method load = parser.getDeclaredMethod("loadShaderAsset", ResourceLocation.class);
        load.setAccessible(true);
        String source = (String) load.invoke(null, ResourceLocation.parse("voxy:lod/pos_util.glsl"));
        require(source.contains("packedPos.y & 15u"), "Actual shader loader did not receive extended Y decoding");
        VoxyShaderRegression.check(source, pack);
        var context = Class.forName("me.cortex.voxy.common.config.ConfigBuildCtx").getConstructor().newInstance();
        var original = Path.of("voxy-regression-cache");
        Files.createDirectories(original);
        var sentinel = original.resolve("original-cache-marker");
        Files.writeString(sentinel, "preserve");
        String isolated = (String) context.getClass().getMethod("ensurePathExists", String.class).invoke(context, original.toString());
        require(Path.of(isolated).equals(original.resolve(VoxyCoordinates.CACHE_DIRECTORY)), "Extended cache was not isolated");
        require(Files.readString(sentinel).equals("preserve"), "Original cache was changed");
        System.out.println("Voxy regression: real NodeManager accepts crash coordinates; 4096 GPU decodes and cache isolation passed");
    }

    public static boolean checkRenderer(BlockPos high) throws Exception {
        var holder = Class.forName("me.cortex.voxy.client.core.IGetVoxyRenderSystem");
        var renderer = holder.getMethod("getNullable").invoke(null);
        require(renderer != null, "Voxy renderer must remain enabled");
        var async = field(renderer, "nodeManager");
        require(field(async, "uncaughtException") == null, "Voxy async renderer failed");
        require(((Thread) field(async, "thread")).isAlive(), "Voxy async renderer stopped");
        var engine = renderer.getClass().getMethod("getEngine").invoke(renderer);
        var section = engine.getClass().getMethod("acquireIfExists", int.class, int.class, int.class, int.class)
            .invoke(engine, 0, high.getX() >> 5, high.getY() >> 5, high.getZ() >> 5);
        if (section == null) return false;
        try {
            require(section.getClass().getField("y").getInt(section) == high.getY() >> 5, "Voxy high section wrapped vertically");
            long[] data = (long[]) section.getClass().getMethod("copyData").invoke(section);
            long voxel = data[((high.getY() & 31) << 10) | ((high.getZ() & 31) << 5) | (high.getX() & 31)];
            var mapper = engine.getClass().getMethod("getMapper").invoke(engine);
            int id = (int) mapper.getClass().getMethod("getBlockId", long.class).invoke(null, voxel);
            var state = (BlockState) mapper.getClass().getMethod("getBlockStateFromBlockId", int.class).invoke(mapper, id);
            boolean captured = state.is(Blocks.DIAMOND_BLOCK);
            if (captured) System.out.println("Voxy regression: enabled renderer captured the diamond block at Y=" + high.getY());
            return captured;
        } finally {
            section.getClass().getMethod("release").invoke(section);
        }
    }

    private static Object field(Object instance, String name) throws Exception {
        var field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static long legacyKey(int level, int x, int y, int z) {
        return ((long) level << 60) | ((long) (y & 255) << 52) |
            ((long) (z & 0xffffff) << 28) | ((long) (x & 0xffffff) << 4);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
