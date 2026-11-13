package dev.jensh.dynamicheightcompat.test.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import dev.jensh.dynamicheightcompat.test.FakeWorldRegression;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Enforce the production failure's wrapper order: replace the cache without invoking inner wrappers. */
@Mixin(value = ServerLevel.class, priority = 2000)
public abstract class ReplacementCacheMixin {
    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"))
    private ServerChunkCache replaceFakeCache(
            ServerLevel level, LevelStorageSource.LevelStorageAccess storage, DataFixer fixer,
            StructureTemplateManager templates, Executor executor, ChunkGenerator generator,
            int viewDistance, int simulationDistance, boolean sync,
            ChunkProgressListener progress, ChunkStatusUpdateListener status,
            Supplier<DimensionDataStorage> dataStorage, Operation<ServerChunkCache> original) {
        if (level instanceof FakeServerLevel) {
            FakeWorldRegression.REPLACEMENT_CALLS.incrementAndGet();
            return FakeServerLevel.createDummyChunkCache(level, storage, fixer, templates, executor,
                generator, viewDistance, simulationDistance, sync, progress, status, dataStorage);
        }
        return original.call(level, storage, fixer, templates, executor, generator,
            viewDistance, simulationDistance, sync, progress, status, dataStorage);
    }
}
