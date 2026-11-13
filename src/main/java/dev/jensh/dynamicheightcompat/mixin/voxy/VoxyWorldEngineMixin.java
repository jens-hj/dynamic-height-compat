package dev.jensh.dynamicheightcompat.mixin.voxy;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.jensh.dynamicheightcompat.VoxyCoordinates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "me.cortex.voxy.common.world.WorldEngine", remap = false)
public abstract class VoxyWorldEngineMixin {
    @ModifyReturnValue(method = "getWorldSectionId", at = @At("RETURN"))
    private static long dynamicHeightCompat$extendY(long key, int level, int x, int y, int z) {
        return VoxyCoordinates.extend(key, y);
    }

    @ModifyReturnValue(method = "getY", at = @At("RETURN"))
    private static int dynamicHeightCompat$decodeY(int ignored, long key) {
        return VoxyCoordinates.y(key);
    }
}
