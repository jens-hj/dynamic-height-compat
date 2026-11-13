package dev.jensh.dynamicheightcompat.mixin.voxy;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.jensh.dynamicheightcompat.VoxyCoordinates;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.gl.shader.ShaderLoader$ShaderLoadingParser", remap = false)
public abstract class VoxyShaderMixin {
    @ModifyReturnValue(method = "loadShaderAsset", at = @At("RETURN"))
    private static String dynamicHeightCompat$decodeExtendedY(String source, ResourceLocation id) {
        if (id.getNamespace().equals("voxy") && id.getPath().equals("lod/pos_util.glsl")) {
            return VoxyCoordinates.patchShader(source);
        }
        return source;
    }
}
