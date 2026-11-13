package dev.jensh.dynamicheightcompat.mixin.voxy;

import dev.jensh.dynamicheightcompat.VoxyCoordinates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "me.cortex.voxy.common.config.ConfigBuildCtx", remap = false)
public abstract class VoxyStorageMixin {
    @ModifyVariable(method = "ensurePathExists", at = @At("HEAD"), argsOnly = true)
    private String dynamicHeightCompat$separateCacheFormat(String path) {
        return VoxyCoordinates.cachePath(path);
    }
}
