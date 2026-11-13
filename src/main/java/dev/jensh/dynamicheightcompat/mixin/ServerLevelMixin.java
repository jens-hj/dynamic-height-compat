package dev.jensh.dynamicheightcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ConstructionGeneratorAccess {
    @Unique private ChunkGenerator dynamicHeightCompat$constructionGenerator;

    @Override
    public ChunkGenerator dynamicHeightCompat$getConstructionGenerator() {
        return dynamicHeightCompat$constructionGenerator;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/dimension/LevelStem;generator()Lnet/minecraft/world/level/chunk/ChunkGenerator;"))
    private ChunkGenerator dynamicHeightCompat$rememberGenerator(LevelStem stem, Operation<ChunkGenerator> original) {
        // Capture before cache construction can be replaced by Moonlight or another mod.
        // Wrapping NEW ServerChunkCache is insufficient: a replacement may bypass that entire call chain.
        var generator = original.call(stem);
        dynamicHeightCompat$constructionGenerator = generator;
        return generator;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dynamicHeightCompat$clearConstructionGenerator(CallbackInfo callback) {
        // State belongs to this level only; failed constructors leave no global/thread-local references.
        dynamicHeightCompat$constructionGenerator = null;
    }
}
