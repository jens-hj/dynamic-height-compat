package dev.jensh.dynamicheightcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ConstructionGeneratorAccess {
    @Unique private ChunkGenerator dynamicHeightCompat$constructionGenerator;

    @Override
    public ChunkGenerator dynamicHeightCompat$getConstructionGenerator() {
        return dynamicHeightCompat$constructionGenerator;
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"))
    private ServerChunkCache dynamicHeightCompat$rememberGenerator(
            ServerLevel level, LevelStorageSource.LevelStorageAccess storage, DataFixer fixer,
            StructureTemplateManager templates, Executor executor, ChunkGenerator generator,
            int viewDistance, int simulationDistance, boolean sync,
            ChunkProgressListener progress, ChunkStatusUpdateListener status,
            Supplier<DimensionDataStorage> dataStorage, Operation<ServerChunkCache> original) {
        // Sable allocates its empty chunk inside this call, before ServerLevel.chunkSource is assigned.
        // Keep the exact generator for that window, including modded and runtime-created dimensions.
        dynamicHeightCompat$constructionGenerator = generator;
        try {
            return original.call(level, storage, fixer, templates, executor, generator,
                viewDistance, simulationDistance, sync, progress, status, dataStorage);
        } finally {
            dynamicHeightCompat$constructionGenerator = null;
        }
    }
}
