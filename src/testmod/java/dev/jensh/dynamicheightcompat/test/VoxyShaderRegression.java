package dev.jensh.dynamicheightcompat.test;

import dev.jensh.dynamicheightcompat.VoxyCoordinates;
import java.lang.reflect.Method;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryUtil;

/** Run the actual loaded decoder on the GPU, including signed Y and spare-bit decoding. */
public final class VoxyShaderRegression {
    private VoxyShaderRegression() {}

    public static void check(String decoder, Method pack) throws Exception {
        int count = 4096;
        var input = MemoryUtil.memAllocInt(count * 2);
        var output = MemoryUtil.memAllocInt(count * 4);
        int previousProgram = GL43C.glGetInteger(GL43C.GL_CURRENT_PROGRAM);
        int previousBuffer = GL43C.glGetInteger(GL43C.GL_SHADER_STORAGE_BUFFER_BINDING);
        int previousInput = GL43C.glGetIntegeri(GL43C.GL_SHADER_STORAGE_BUFFER_BINDING, 0);
        int previousOutput = GL43C.glGetIntegeri(GL43C.GL_SHADER_STORAGE_BUFFER_BINDING, 1);
        int shader = GL43C.glCreateShader(GL43C.GL_COMPUTE_SHADER);
        int program = GL43C.glCreateProgram();
        int inBuffer = GL43C.glGenBuffers();
        int outBuffer = GL43C.glGenBuffers();
        try {
            for (int y = -2048; y < 2048; y++) {
                long key = (long) pack.invoke(null, (y + 2048) % 5, -864, y, 416);
                input.put((int) (key >>> 32)).put((int) key);
            }
            input.flip();
            GL43C.glShaderSource(shader, "#version 430\n" + decoder + """

                layout(local_size_x=32) in;
                layout(std430, binding=0) readonly buffer InputKeys { uvec2 keys[]; };
                layout(std430, binding=1) writeonly buffer OutputValues { ivec4 values[]; };
                void main() {
                    uint i = gl_GlobalInvocationID.x;
                    values[i] = ivec4(getLoDPosition(keys[i]), int(getLoDLevel(keys[i])));
                }
                """);
            GL43C.glCompileShader(shader);
            require(GL43C.glGetShaderi(shader, GL43C.GL_COMPILE_STATUS) != 0,
                "Voxy decoder shader failed: " + GL43C.glGetShaderInfoLog(shader));
            GL43C.glAttachShader(program, shader);
            GL43C.glLinkProgram(program);
            require(GL43C.glGetProgrami(program, GL43C.GL_LINK_STATUS) != 0,
                "Voxy decoder link failed: " + GL43C.glGetProgramInfoLog(program));
            GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, inBuffer);
            GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, input, GL43C.GL_STATIC_DRAW);
            GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, 0, inBuffer);
            GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, outBuffer);
            GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, (long) count * 16, GL43C.GL_STREAM_READ);
            GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, 1, outBuffer);
            GL43C.glUseProgram(program);
            GL43C.glDispatchCompute(count / 32, 1, 1);
            GL43C.glMemoryBarrier(GL43C.GL_BUFFER_UPDATE_BARRIER_BIT);
            GL43C.glGetBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, 0, output);
            for (int y = -2048; y < 2048; y++) {
                int i = (y + 2048) * 4;
                require(output.get(i) == -864 && output.get(i + 1) == y && output.get(i + 2) == 416 &&
                    output.get(i + 3) == (y + 2048) % 5, "GPU Voxy coordinate mismatch at section Y=" + y);
            }
        } finally {
            GL43C.glUseProgram(previousProgram);
            GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, 0, previousInput);
            GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, 1, previousOutput);
            GL43C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, previousBuffer);
            GL43C.glDeleteBuffers(inBuffer);
            GL43C.glDeleteBuffers(outBuffer);
            GL43C.glDeleteProgram(program);
            GL43C.glDeleteShader(shader);
            MemoryUtil.memFree(input);
            MemoryUtil.memFree(output);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
