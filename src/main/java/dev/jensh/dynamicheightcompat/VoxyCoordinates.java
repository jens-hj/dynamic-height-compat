package dev.jensh.dynamicheightcompat;

import java.nio.file.Path;

/** Extends Voxy's Y coordinate using its four spare key bits; ordinary keys stay unchanged. */
public final class VoxyCoordinates {
    public static final String CACHE_DIRECTORY = "dynamic-height-compat-y12-v1";
    private static final String SHADER_Y = "int y = ((int(packedPos.x)<<4)>>24);";
    private static final String SHADER_EXTENSION =
        "\n    y = ((y ^ (int(packedPos.y & 15u) << 8)) << 20) >> 20;";

    private VoxyCoordinates() {}

    public static long extend(long original, int y) {
        if (y < -2048 || y > 2047) throw new IllegalArgumentException("Voxy section Y outside 12-bit range: " + y);
        // XOR with the old signed byte preserves every legacy key in -128..127.
        return original | (((y ^ (byte) y) >>> 8) & 15L);
    }

    public static int y(long key) {
        int oldY = (byte) (key >>> 52);
        return ((oldY ^ ((int) (key & 15) << 8)) << 20) >> 20;
    }

    public static String patchShader(String source) {
        if (source.indexOf(SHADER_Y) < 0 || source.indexOf(SHADER_Y) != source.lastIndexOf(SHADER_Y)) {
            throw new IllegalStateException("Unsupported Voxy position shader: expected one Y decoder");
        }
        return source.replace(SHADER_Y, SHADER_Y + SHADER_EXTENSION);
    }

    public static String cachePath(String original) {
        return Path.of(original).resolve(CACHE_DIRECTORY).toString();
    }
}
