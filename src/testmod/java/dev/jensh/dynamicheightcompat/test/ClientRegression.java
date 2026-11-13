package dev.jensh.dynamicheightcompat.test;

import com.google.gson.Gson;
import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.multiverse.dynamicheight.worldheight.WorldHeightData;
import net.multiverse.dynamicheight.client.WorldHeightSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Only loaded from the development test source set, never included in the addon jar. */
@Mod(value = "dynamic_height_compat_test", dist = Dist.CLIENT)
public final class ClientRegression {
    private static final String WORLD = "compat-height-test";
    private static final BlockPos HIGH = new BlockPos(0, 512, 0);
    private static final BlockPos LOW = new BlockPos(0, -96, 0);
    private boolean started, verified, done;
    private int ticks, settle;

    public ClientRegression() {
        if (Boolean.getBoolean("dynamicheightcompat.clientTest")) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> tick());
        }
    }

    private void tick() {
        if (done) return;
        var mc = Minecraft.getInstance();
        try {
            if (++ticks > 4800) throw new AssertionError("Client test timed out at " + mc.screen);
            if (mc.screen instanceof net.neoforged.neoforge.client.gui.LoadingErrorScreen) {
                throw new AssertionError("NeoForge mod loading failed");
            }
            boolean reload = Boolean.getBoolean("dynamicheightcompat.reload");
            if (!started) {
                if (!(mc.screen instanceof TitleScreen)) return;
                started = true;
                if (reload) {
                    mc.createWorldOpenFlows().openWorld(WORLD, () -> finish(false, "World reload cancelled"));
                } else {
                    // The direct world-opening flow logs out first and clears DH's pending UI selection.
                    // Seed its documented per-world config, then verify the actual saved bounds on reload.
                    var config = Path.of("saves", WORLD, "data", "dynamic_height_minecraft_overworld.config");
                    Files.createDirectories(config.getParent());
                    Files.writeString(config,
                        "{\"format\":\"dynamicheight-worldheight-v1\",\"minY\":-128,\"maxY\":1024}\n");
                    WorldHeightSettings.setMinY(-128);
                    WorldHeightSettings.setMaxY(1024);
                    WorldHeightData.updateFromClient(-128, 1024);
                    mc.createWorldOpenFlows().createFreshLevel(WORLD,
                        new LevelSettings("Compat regression", GameType.CREATIVE, false,
                            Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(0, false, false),
                        registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
                }
                return;
            }
            var server = mc.getSingleplayerServer();
            if (mc.level == null || mc.player == null || server == null) return;
            if (!verified) {
                CompletableFuture.runAsync(() -> {
                    var level = server.overworld();
                    require(level.getMinBuildHeight() == -128 && level.getMaxBuildHeight() == 1024,
                        "Extended world bounds were not preserved: " + level.getMinBuildHeight() + ".." + level.getMaxBuildHeight());
                    require(((ConstructionGeneratorAccess) level).dynamicHeightCompat$getConstructionGenerator() == null,
                        "Construction generator leaked after startup");
                    if (reload) {
                        require(level.getBlockState(HIGH).is(Blocks.DIAMOND_BLOCK), "High block did not survive reload");
                        require(level.getBlockState(LOW).is(Blocks.GOLD_BLOCK), "Low block did not survive reload");
                    } else {
                        level.setBlock(HIGH, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
                        level.setBlock(LOW, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                        require(level.getBlockState(HIGH).is(Blocks.DIAMOND_BLOCK), "Could not place above vanilla build height");
                        require(level.getBlockState(LOW).is(Blocks.GOLD_BLOCK), "Could not place below vanilla build height");
                    }
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
                        "tp @a 0.5 514 0.5");
                    server.saveEverything(true, true, true);
                }, server).join();
                verified = true;
                mc.setScreen(null);
                return;
            }
            if (++settle < 100) return;
            require(mc.level.getMinBuildHeight() == -128 && mc.level.getMaxBuildHeight() == 1024,
                "Client did not receive extended bounds");
            if (!mc.level.hasChunk(0, 0)) return;
            require(mc.level.getBlockState(HIGH).is(Blocks.DIAMOND_BLOCK), "Client did not receive high block");
            finish(true, reload ? "Extended world reload and persisted blocks passed" :
                "Extended world startup, placement at Y=512/-96, and client sync passed");
        } catch (Throwable error) {
            error.printStackTrace();
            finish(false, error.toString());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private void finish(boolean passed, String message) {
        done = true;
        try {
            var path = Path.of(System.getProperty("dynamicheightcompat.testResult"));
            Files.createDirectories(path.getParent());
            Files.writeString(path, new Gson().toJson(Map.of("passed", passed, "message", message)));
        } catch (Exception error) { throw new RuntimeException(error); }
        Minecraft.getInstance().stop();
    }
}
