package net.frogmouth.rnd.yuv;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
            OutputFormat outputFormat = new OutputFormat_RGB_Bytes(reader.getFrameWidth() * reader.getFrameHeight());
            byte[] rgbData = ColourSpaceConverter.YuvConverter(reader, frameData, outputFormat);
            Files.write(rgbOutputFile.toPath(), rgbData);
        }
        assertEquals(52, numFrames);
    }
}
