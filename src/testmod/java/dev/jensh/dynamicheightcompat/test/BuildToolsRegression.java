package dev.jensh.dynamicheightcompat.test;

import dev.jensh.dynamicheightcompat.BuildToolsCoordinates;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

public final class BuildToolsRegression {
    private BuildToolsRegression() {}

    public static void check(ServerLevel level) {
        var source = List.of("forceload add -20000000 100 -20000000 100",
            "$setblock -20000000 -64 96 $(block)[axis=x]", "# -20000000",
            "say -200000001 ~-20000000 -20000000.5");
        var result = BuildToolsCoordinates.rewrite(ResourceLocation.parse("jbt:regression"), source);
        require(result.equals(List.of("forceload add -2000000 100 -2000000 100",
            "$setblock -2000000 -64 96 $(block)[axis=x]", source.get(2), source.get(3))),
            "JBT commands/macros must move together without rewriting unrelated numeric tokens");
        require(BuildToolsCoordinates.rewrite(ResourceLocation.parse("other:regression"), source) == source,
            "Other namespaces must remain untouched");
        var helper = new BlockPos(BuildToolsCoordinates.HELPER_X, -64, 96);
        require(BlockPos.of(helper.asLong()).equals(helper), "Helper must round-trip through DH packing");
        require(BlockPos.of(BlockPos.asLong(-20_000_000, -64, 96)).getX() != -20_000_000,
            "Regression must exercise the real Dynamic Height horizontal packing limit");
        var server = level.getServer();
        require(server.getFunctions().get(ResourceLocation.parse("jbt:loaded")).isPresent(),
            "The real JBT load function must be present in the integration runtime");
        for (var dimension : server.getAllLevels()) {
            require(dimension.getForcedChunks().contains(ChunkPos.asLong(BuildToolsCoordinates.HELPER_X >> 4, 100 >> 4)),
                "JBT load function did not force the relocated helper in " + dimension.dimension().location());
            require(!dimension.getForcedChunks().contains(ChunkPos.asLong(-20_000_000 >> 4, 100 >> 4)),
                "JBT still forces an unrepresentable chunk");
        }
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
            "function jbt:rotation/get/xyz {block:\"minecraft:oak_log\"}");
        require(level.getBlockState(helper).is(Blocks.OAK_LOG), "JBT macro did not use the relocated scratch block");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
