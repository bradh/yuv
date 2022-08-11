package net.frogmouth.rnd.yuv;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class OutputFormat_RGB_Bytes implements OutputFormat {

    private final ByteBuffer outputByteBuffer;

    public OutputFormat_RGB_Bytes(int numPixels) {
        outputByteBuffer = ByteBuffer.allocate(numPixels * 3);
    }

    @Override
    public void putRGB(int r, int g, int b) {
        try {
            outputByteBuffer.put(clampToUnsignedByte(r));
            outputByteBuffer.put(clampToUnsignedByte(g));
            outputByteBuffer.put(clampToUnsignedByte(b));
        } catch (BufferOverflowException ex) {
            System.out.println(ex.toString());
        }
    }

    @Override
    public byte[] getBytes() {
        return outputByteBuffer.array();
    }

    private static byte clampToUnsignedByte(int v) {
        return (byte) (clamp(v, 0, 255) & 0xFF);
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

}
