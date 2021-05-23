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
package demoapp.dom.types.javaawt.images.samples;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class JavaAwtBufferedImageService {

    @SneakyThrows
    public BufferedImage bytesToJavaAwtBufferedImage(byte[] bytes) {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @SneakyThrows
    public int[][] javaAwtImageToPixels(final BufferedImage image) {
        final int width = image.getWidth(null);
        final int lines = image.getHeight(null);
        final int pixels[] = new int[width * lines];
        final PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, lines, pixels, 0, width);
        grabber.setColorModel(ColorModel.getRGBdefault());
        if (grabber.grabPixels() && (grabber.status() & ImageObserver.ALLBITS) != 0) {
            final int[][] array = new int[lines][width];
            int srcPos = 0;
            for (int line = 0; line < lines; line++) {
                array[line] = new int[width];
                System.arraycopy(pixels, srcPos, array[line], 0, width);
                for (int pixel = 0; pixel < array[line].length; pixel++) {
                    array[line][pixel] = array[line][pixel] | 0xFF000000;
                }
                srcPos += width;
            }
            return array;
        }
        return new int[lines][width];
    }


}
