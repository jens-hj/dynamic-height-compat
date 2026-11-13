package dev.jensh.dynamicheightcompat.mixin;

import dev.jensh.dynamicheightcompat.ConstructionGeneratorAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.multiverse.dynamicheight.vertical.GenerationBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenerationBounds.class, remap = false)
public abstract class GenerationBoundsMixin {
    @Inject(method = "from", at = @At("HEAD"), cancellable = true)
    private static void dynamicHeightCompat$useConstructionGenerator(LevelHeightAccessor accessor,
            CallbackInfoReturnable<GenerationBounds> result) {
        if (accessor instanceof ServerLevel level && level.getChunkSource() == null &&
                level instanceof ConstructionGeneratorAccess construction) {
            var generator = construction.dynamicHeightCompat$getConstructionGenerator();
            if (generator != null) {
                result.setReturnValue(new GenerationBounds(generator.getMinY(), generator.getGenDepth()));
            }
        }
    }
}
