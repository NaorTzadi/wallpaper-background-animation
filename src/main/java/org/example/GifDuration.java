package org.example;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
public class GifDuration {
    public static int getGifDuration(String gifFilePath) {
        File file = new File(gifFilePath);
        try (ImageInputStream stream = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                throw new RuntimeException("No GIF image readers available");
            }
            ImageReader reader = readers.next();
            reader.setInput(stream);
            int numberOfFrames = reader.getNumImages(true);
            int duration = 0;

            for (int i = 0; i < numberOfFrames; i++) {
                IIOMetadata metadata = reader.getImageMetadata(i);
                String metaFormat = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

                NodeList children = root.getElementsByTagName("GraphicControlExtension");
                for (int j = 0; j < children.getLength(); j++) {
                    IIOMetadataNode child = (IIOMetadataNode) children.item(j);
                    String delayTime = child.getAttribute("delayTime");
                    duration += Integer.parseInt(delayTime);
                }
            }
            duration *= 10;
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
