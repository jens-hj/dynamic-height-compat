package dev.jensh.dynamicheightcompat;

import net.minecraft.world.level.chunk.ChunkGenerator;

/** The actual generator supplied to a level's chunk-cache constructor, only while it runs. */
public interface ConstructionGeneratorAccess {
    ChunkGenerator dynamicHeightCompat$getConstructionGenerator();
}
