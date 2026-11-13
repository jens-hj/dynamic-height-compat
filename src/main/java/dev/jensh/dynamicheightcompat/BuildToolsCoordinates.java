package dev.jensh.dynamicheightcompat;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

/** Keep JBT's private scratch area within DH's signed 24-bit horizontal range. */
public final class BuildToolsCoordinates {
    public static final int HELPER_X = -2_000_000;
    private static final Pattern OLD_X = Pattern.compile("(?<!\\S)-20000000(?!\\S)");

    private BuildToolsCoordinates() {}

    public static List<String> rewrite(ResourceLocation function, List<String> lines) {
        if (!function.getNamespace().equals("jbt")) return lines;
        return lines.stream().map(line -> line.stripLeading().startsWith("#") ? line :
            OLD_X.matcher(line).replaceAll(Integer.toString(HELPER_X))).toList();
    }
}
