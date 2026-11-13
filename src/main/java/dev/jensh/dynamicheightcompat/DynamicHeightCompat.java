package dev.jensh.dynamicheightcompat;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;

@Mod(DynamicHeightCompat.ID)
public final class DynamicHeightCompat {
    public static final String ID = "dynamic_height_compat";

    public DynamicHeightCompat() {
        LogUtils.getLogger().info("Dynamic Height Compat: generator bounds startup fix {}",
            Boolean.getBoolean("dynamicheightcompat.test.disableStartupFix") ? "disabled (test baseline)" : "enabled");
        LogUtils.getLogger().info("Dynamic Height Compat: Jake's Build Tools helper relocation {}",
            Boolean.getBoolean("dynamicheightcompat.test.disableBuildToolsFix") ? "disabled (test baseline)" : "enabled");
    }
}
