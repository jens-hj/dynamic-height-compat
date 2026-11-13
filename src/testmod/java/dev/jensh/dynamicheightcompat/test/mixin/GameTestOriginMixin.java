package dev.jensh.dynamicheightcompat.test.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Vanilla randomly selects ±15M, beyond DH's ±8M packing range. Test at a valid origin. */
@Mixin(GameTestServer.class)
public abstract class GameTestOriginMixin {
    @ModifyVariable(method = "startTests", at = @At("STORE"), ordinal = 0)
    private BlockPos dynamicHeightCompat$useRepresentableOrigin(BlockPos randomOrigin) {
        return new BlockPos(0, -59, 0);
    }
}
