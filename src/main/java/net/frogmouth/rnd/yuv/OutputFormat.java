package net.frogmouth.rnd.yuv;

import java.nio.ByteBuffer;

/**
 *
 * @author bradh
 */
public interface OutputFormat {

    public void putRGB(int r, int g, int b);

    public byte[] getBytes();
    
    
}
