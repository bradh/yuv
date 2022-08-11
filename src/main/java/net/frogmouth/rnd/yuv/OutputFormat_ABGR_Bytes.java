package net.frogmouth.rnd.yuv;

import java.nio.ByteBuffer;

public class OutputFormat_ABGR_Bytes implements OutputFormat {

    private final ByteBuffer outputByteBuffer;

    public OutputFormat_ABGR_Bytes(int numPixels) {
        outputByteBuffer = ByteBuffer.allocate(numPixels * 4);
    }

    @Override
    public void putRGB(int r, int g, int b) {
        putABGR(b, g, r);
    }

    @Override
    public byte[] getBytes() {
        return outputByteBuffer.array();
    }

    private void putABGR(int b, int g, int r) {
        outputByteBuffer.put((byte) 0xFF);
        outputByteBuffer.put(clampToUnsignedByte(b));
        outputByteBuffer.put(clampToUnsignedByte(g));
        outputByteBuffer.put(clampToUnsignedByte(r));
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
