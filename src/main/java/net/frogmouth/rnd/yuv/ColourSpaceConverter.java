package net.frogmouth.rnd.yuv;

import java.nio.BufferOverflowException;

public class ColourSpaceConverter {

    public static byte[] YuvConverter(Y4mReader reader, byte[] frameData, OutputFormat outputFormat) {
        int numPixels = reader.getFrameHeight() * reader.getFrameWidth();
        int horizontalSubsampling = reader.getColourSpace().getHorizontalSubsampling();
        int verticalSubsampling = reader.getColourSpace().getVerticalSubsampling();
        for (int y = 0; y < reader.getFrameHeight(); y++) {
            for (int x = 0; x < reader.getFrameWidth(); x++) {
                try {
                    int yIndex = y * reader.getFrameWidth() + x;
                    int yValue = frameData[yIndex] & 0xFF;
                    int uvIndex = (y / (verticalSubsampling * horizontalSubsampling) * reader.getFrameWidth()) + x / horizontalSubsampling;
                    int uValue = (frameData[numPixels + uvIndex] & 0xFF) - 128;
                    int vValue = (frameData[numPixels + numPixels / (horizontalSubsampling * verticalSubsampling) + uvIndex] & 0xFF) - 128;
                    int r = (int) (yValue + 1.370705f * vValue);
                    int g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
                    int b = (int) (yValue + 1.732446f * uValue);
                    outputFormat.putRGB(r, g, b);
                } catch (BufferOverflowException ex) {
                    System.out.println(ex.toString());
                }

            }
        }
        return outputFormat.getBytes();
    }
}
