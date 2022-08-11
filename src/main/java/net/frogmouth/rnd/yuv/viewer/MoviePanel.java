package net.frogmouth.rnd.yuv.viewer;

import net.frogmouth.rnd.yuv.OutputFormat;
import net.frogmouth.rnd.yuv.OutputFormat_ABGR_Bytes;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import net.frogmouth.rnd.yuv.Y4mReader;
import static net.frogmouth.rnd.yuv.ColourSpaceConverter.YuvConverter;

public class MoviePanel extends JPanel {

    private final int B_WIDTH = 1930;
    private final int B_HEIGHT = 1090;
    private final int INITIAL_DELAY = 0;

    private BufferedImage image;
    private Timer timer;
    private Y4mReader reader;

    public MoviePanel() throws IOException {
        startMovie();
    }

    private void loadImage() throws IOException {
        OutputFormat outputFormat = new OutputFormat_ABGR_Bytes(reader.getFrameWidth() * reader.getFrameHeight());
        if (reader.hasMoreFrames()) {
            byte[] frameData = reader.getFrame();
            System.out.println(frameData.length);
            byte[] abgrData = YuvConverter(reader, frameData, outputFormat);
            byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(abgrData, 0, imgData, 0, abgrData.length);
        } else {
            timer.cancel();
        }
    }

    private void startMovie() throws IOException {
        // File y4m = new File("/home/bradh/NetBeansProjects/yuv/src/test/resources/example.y4m");
        // File y4m = new File("/home/bradh/yuvdata/controlled_burn_1080p.y4m");
        // File y4m = new File("/home/bradh/yuvdata/pedestrian_area_1080p25.y4m");
        File y4m = new File("/home/bradh/yuvdata/in_to_tree_444_720p50.y4m");
        // File y4m = new File("/home/bradh/yuvdata/ducks_take_off_444_720p50.y4m");
        // File y4m = new File("/home/bradh/yuvdata/park_joy_444_720p50.y4m");
        SeekableByteChannel fileChannel = FileChannel.open(y4m.toPath(), StandardOpenOption.READ);

        reader = new Y4mReader(fileChannel);
        reader.readHeader();
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        image = new BufferedImage(reader.getFrameWidth(), reader.getFrameHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        int periodInterval = 1000 * reader.getFrameRateDenominator() / reader.getFrameRateNumerator();
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(),
                INITIAL_DELAY, periodInterval);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    private class ScheduleTask extends TimerTask {

        @Override
        public void run() {
            try {
                loadImage();
                repaint();
            } catch (IOException ex) {
                Logger.getLogger(MoviePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
