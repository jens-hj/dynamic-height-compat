package dev.jensh.dynamicheightcompat.test;

import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeServerLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.multiverse.dynamicheight.vertical.GenerationBounds;

/** Exercises Moonlight's replacement cache constructor, which bypasses the vanilla NEW operation. */
public final class FakeWorldRegression {
    public static final AtomicInteger REPLACEMENT_CALLS = new AtomicInteger();

    public static void check(ServerLevel parent) {
        int previousReplacements = REPLACEMENT_CALLS.get();
        try (var fake = new FakeServerLevel("dynamic_height_compat:regression", parent)) {
            require(REPLACEMENT_CALLS.get() > previousReplacements, "Constructor replacement regression fixture did not run");
            var generator = parent.getChunkSource().getGenerator();
            var bounds = GenerationBounds.from(fake);
            require(fake.getChunkSource().getGenerator() == generator, "Fake world must retain its parent's generator");
            require(bounds.minY() == generator.getMinY() && bounds.height() == generator.getGenDepth(),
                "Fake world generator bounds changed");
            require(fake.getChunk(0, 0) instanceof EmptyLevelChunk, "Moonlight replacement cache was not used");
            require(((ConstructionGeneratorAccess) fake).dynamicHeightCompat$getConstructionGenerator() == null,
                "Fake world's temporary construction generator was not cleared");
            require(((ConstructionGeneratorAccess) parent).dynamicHeightCompat$getConstructionGenerator() == null,
                "Fake world construction changed the parent's temporary generator");
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
