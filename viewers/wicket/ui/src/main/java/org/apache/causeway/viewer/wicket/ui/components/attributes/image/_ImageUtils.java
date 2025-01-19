package org.apache.causeway.viewer.wicket.ui.components.attributes.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class _ImageUtils {

    String getName(BufferedImage image) {
        return "Image";
    }
    
    @SneakyThrows
    String getMimeType(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos); // Use a default format to write the image
            byte[] imageData = baos.toByteArray();
            return getMimeTypeFromBytes(imageData);
        }
    }

    String getMimeTypeFromBytes(byte[] imageData) {
        if (imageData.length >= 2) {
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return "image/jpeg";
            } else if (imageData[0] == (byte) 0x89 && Arrays.equals(imageData, 0, 4, new byte[]{0x50, 0x4E, 0x47, 0x0D}, 0, 4)) {
                return "image/png";
            }
        }
        return "application/octet-stream"; // Default MIME type
    }

    @SneakyThrows
    byte[] getImageData(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos); // Use the same format as above
            return baos.toByteArray();
        }
    }

}
