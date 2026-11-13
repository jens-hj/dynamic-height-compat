package dev.jensh.dynamicheightcompat;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** The switch exists solely for automated negative-control runs; fixes are enabled by default. */
public final class CompatMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".voxy.")) {
            return !Boolean.getBoolean("dynamicheightcompat.test.disableVoxyFix") &&
                FMLLoader.getLoadingModList().getModFileById("voxy") != null;
        }
        if (mixinClassName.endsWith("CommandFunctionMixin")) {
            return !Boolean.getBoolean("dynamicheightcompat.test.disableBuildToolsFix");
        }
        return !Boolean.getBoolean("dynamicheightcompat.test.disableStartupFix");
    }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
