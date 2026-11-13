package dev.jensh.dynamicheightcompat;

import net.minecraft.world.level.chunk.ChunkGenerator;

/** The level's actual generator, available until its constructor completes. */
public interface ConstructionGeneratorAccess {
    ChunkGenerator dynamicHeightCompat$getConstructionGenerator();
}
