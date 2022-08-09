package net.frogmouth.rnd.yuv;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Y4mReader.
 */
public class Y4mReaderTest {

    @Test
    public void checkHeaderRead() throws IOException, URISyntaxException {
        Path path = Path.of(ClassLoader.getSystemResource("example.y4m").toURI());
        SeekableByteChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);

        Y4mReader reader = new Y4mReader(fileChannel);
        reader.readHeader();
        assertEquals(384, reader.getFrameWidth());
        assertEquals(288, reader.getFrameHeight());
        assertEquals(25, reader.getFrameRateNumerator());
        assertEquals(1, reader.getFrameRateDenominator());
        assertEquals(InterlacingMode.Progressive, reader.getInterlacing());
        assertEquals(PixelAspectRatio.Unknown, reader.getPixelAspectRatio());
        int numFrames = 0;
        while (reader.hasMoreFrames()) {
            byte[] frameData = reader.getFrame();
            numFrames += 1;
            File yuvOutputFile = new File(String.format("frame_%02d.yuv", numFrames));
            Files.write(yuvOutputFile.toPath(), frameData);
            File rgbOutputFile = new File(String.format("frame_%02d.rgb", numFrames));
            byte[] rgbData = Yuv2Rgb(reader, frameData);
            Files.write(rgbOutputFile.toPath(), rgbData);
        }
        assertEquals(52, numFrames);
    }

    private byte[] Yuv2Rgb(Y4mReader reader, byte[] frameData) {
        int numPixels = reader.getFrameHeight() * reader.getFrameWidth();
        ByteBuffer rgb = ByteBuffer.allocate(numPixels * 4);
        for (int y = 0; y < reader.getFrameHeight(); y++) {
            for (int x = 0; x < reader.getFrameWidth(); x++) {
                int yIndex = y * reader.getFrameWidth() + x;
                int yValue = frameData[yIndex] & 0xFF;
                int uvx = x / 2;
                int uvy = y / 2;
                int uvIndex = (uvy * reader.getFrameWidth() / 2) + uvx;
                int uValue = (frameData[numPixels + uvIndex] & 0xFF) - 128;
                int vValue = (frameData[numPixels + numPixels / 4 + uvIndex] & 0xFF) - 128;
                int r = (int)(yValue + 1.370705f * vValue);
                int g = (int)(yValue - (0.698001f * vValue) + (0.337633f * uValue));
                int b = (int)(yValue + 1.732446f * uValue);
                // r = clamp(r, 0, 255);
                // g = clamp(g, 0, 255);
                // b = clamp(b, 0, 255);
                int a = 255;
                // rgb.put((byte) (r & 0xFF));
                // rgb.put((byte) (g & 0xFF));
                // rgb.put((byte) (b & 0xFF));
                rgb.put(clampToUnsignedByte(r));
                rgb.put(clampToUnsignedByte(g));
                rgb.put(clampToUnsignedByte(b));
                rgb.put((byte) (a & 0xFF));
            }
        }
        return rgb.array();
    }

    private byte clampToUnsignedByte(int v) {
        return (byte)(clamp(v, 0, 255) & 0xFF);
    }
    
    private int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }
}
