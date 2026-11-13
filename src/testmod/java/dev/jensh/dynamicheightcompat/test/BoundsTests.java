package dev.jensh.dynamicheightcompat.test;

import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.multiverse.dynamicheight.vertical.GenerationBounds;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("dynamic_height_compat")
@PrefixGameTestTemplate(false)
public final class BoundsTests {
    @GameTest(template = "empty")
    public static void voxyExtendedCoordinatesDoNotCollide(GameTestHelper test) {
        VoxyRegression.checkCodec();
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void buildToolsHelpersUseRepresentableCoordinates(GameTestHelper test) {
        BuildToolsRegression.check(test.getLevel());
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void moonlightFakeWorldStartsWithSable(GameTestHelper test) {
        FakeWorldRegression.check(test.getLevel());
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void worldStartsWithSableAndKeepsGeneratorBounds(GameTestHelper test) {
        var level = test.getLevel();
        var expected = level.getChunkSource().getGenerator();
        var actual = GenerationBounds.from(level);
        test.assertTrue(actual.minY() == expected.getMinY() && actual.height() == expected.getGenDepth(),
            "Initialized levels must keep Dynamic Height's original generator bounds");
        test.assertTrue(((ConstructionGeneratorAccess) level).dynamicHeightCompat$getConstructionGenerator() == null,
            "Temporary construction generator must be cleared");
        test.setBlock(new BlockPos(1, 2, 1), Blocks.GOLD_BLOCK);
        test.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(1, 2, 1));
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void nonServerHeightAccessorsKeepTheirBounds(GameTestHelper test) {
        var actual = GenerationBounds.from(LevelHeightAccessor.create(-1024, 4096));
        test.assertTrue(actual.minY() == -1024 && actual.height() == 4096,
            "Non-server height accessors must remain unchanged");
        test.succeed();
    }
}
