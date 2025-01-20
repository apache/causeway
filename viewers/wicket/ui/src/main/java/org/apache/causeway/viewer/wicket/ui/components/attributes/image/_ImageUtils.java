/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.wicket.ui.components.attributes.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class _ImageUtils {
    
    // -- EXPERIMENTAL STUFF

    String getName(BufferedImage image) {
        return "Image";
    }
    
    @SneakyThrows
    String getMimeType1(BufferedImage image) {
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

//    @SneakyThrows
//    String getMimeType(BufferedImage image) throws IOException {
//        ByteArrayInputStream bais = new ByteArrayInputStream(ImageIO.write(image, "png", new ByteArrayOutputStream()).toByteArray());
//        ImageInputStream iis = ImageIO.createImageInputStream(bais);
//        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
//        if (readers.hasNext()) {
//            ImageReader reader = readers.next();
//            reader.setInput(iis, true);
//            IIOMetadata metadata = reader.getImageMetadata(0);
//            return metadata.getNativeMetadataFormatName();
//        } else {
//            return "application/octet-stream"; // Default MIME type
//        }
//    }

}
