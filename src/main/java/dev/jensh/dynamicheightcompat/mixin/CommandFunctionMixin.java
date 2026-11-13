package dev.jensh.dynamicheightcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import dev.jensh.dynamicheightcompat.BuildToolsCoordinates;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandFunction.class)
public interface CommandFunctionMixin {
    @WrapMethod(method = "fromLines")
    private static <T extends ExecutionCommandSource<T>> CommandFunction<T> dynamicHeightCompat$moveBuildToolsHelpers(
            ResourceLocation id, CommandDispatcher<T> dispatcher, T source, List<String> lines,
            Operation<CommandFunction<T>> original) {
        return original.call(id, dispatcher, source, BuildToolsCoordinates.rewrite(id, lines));
    }
}
