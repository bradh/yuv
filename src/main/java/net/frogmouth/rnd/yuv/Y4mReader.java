package net.frogmouth.rnd.yuv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

public class Y4mReader {

    private final SeekableByteChannel channel;
    private int frameWidth;
    private int frameHeight;
    private int frameRateNumerator;
    private int frameRateDenominator;
    private InterlacingMode interlacing;
    private PixelAspectRatio pixelAspectRatio;

    public Y4mReader(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public void readHeader() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4096);
        channel.read(bb);
        int firstFrameHeaderOffset = findNextNewline(bb);
        if (firstFrameHeaderOffset == 0) {
            throw new IOException("Cannot find end of header bytes in first 4K");
        }
        byte[] headerBytes = new byte[firstFrameHeaderOffset];
        bb.rewind();
        bb.get(headerBytes);
        String headerAsText = new String(headerBytes, StandardCharsets.US_ASCII);
        String[] headers = headerAsText.split(" ");
        if (!"YUV4MPEG2".equals(headers[0])) {
            throw new IOException("Did not find YUV4MPEG2 header signature");
        }
        for (String header : headers) {
            processFileHeader(header);
        }
        channel.position(firstFrameHeaderOffset + 1);
    }
    
    public byte[] getFrame() throws IOException {
        long startPosition = channel.position();
        int frameSizeBytes = getFrameSizeBytes();
        ByteBuffer bb = ByteBuffer.allocate(frameSizeBytes + 4096);
        channel.read(bb);
        int frameHeaderBytes = findNextNewline(bb);
        System.out.println("end of FRAME position: " + frameHeaderBytes);
        // TODO: process per frame headers if we ever find some
        byte[] frameBytes = new byte[frameSizeBytes];
        bb.get(frameHeaderBytes + 1, frameBytes, 0, frameSizeBytes);
        channel.position(startPosition + frameHeaderBytes + frameSizeBytes);
        return frameBytes;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getFrameRateNumerator() {
        return frameRateNumerator;
    }

    int getFrameRateDenominator() {
        return frameRateDenominator;
    }

    public InterlacingMode getInterlacing() {
        return interlacing;
    }

    public PixelAspectRatio getPixelAspectRatio() {
        return pixelAspectRatio;
    }

    private int findNextNewline(ByteBuffer bb) {
        for (int i = 0; i < bb.limit(); i++) {
            byte b = bb.get(i);
            if (b == 0x0A) {
                return i;
            }
        }
        return 0;
    }

    private void processFileHeader(String header) {
        if (header.equals("YUV4MPEG2")) {
            return;
        }
        char firstChar = header.charAt(0);
        String value = header.substring(1);
        switch (firstChar) {
            case 'A':
                this.pixelAspectRatio = PixelAspectRatio.lookup(value);
                break;
            case 'F':
                String[] rationalParts = value.split(":");
                if (rationalParts.length == 2) {
                    this.frameRateNumerator = Integer.parseInt(rationalParts[0]);
                    this.frameRateDenominator = Integer.parseInt(rationalParts[1]);
                }
                break;
            case 'H':
                this.frameHeight = Integer.parseInt(value);
                break;
            case 'I':
                this.interlacing = InterlacingMode.lookup(value);
                break;
            case 'W':
                this.frameWidth = Integer.parseInt(value);
                break;

            default:
                System.out.println("unprocessed header type: " + firstChar);
        }
    }

    private int getFrameSizeBytes() {
        // TODO: handle cases where it isn't 420
        return frameWidth * frameHeight * 3 / 2;
    }

    boolean hasMoreFrames() throws IOException {
        return channel.position() < channel.size();
    }

}
