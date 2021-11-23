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
package org.apache.isis.commons.internal.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.primitives._Ints;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class _Images {

    // -- BYTES

    @SneakyThrows
    public static byte[] toBytes(final @NonNull BufferedImage image){
        try(val bos = new ByteArrayOutputStream(8 * 1024)) {
            ImageIO.write(image, "png", bos); // png is lossless
            return bos.toByteArray();
        }
    }

    @SneakyThrows
    public static BufferedImage fromBytes(final @NonNull byte[] imageData){
        try(val bis = new ByteArrayInputStream(imageData)){
            return ImageIO.read(bis);
        }
    }

    // -- BASE64

    @SneakyThrows
    public static String toBase64(final @NonNull BufferedImage image){
        return new String(_Bytes.asUrlBase64.apply(toBytes(image)),
                StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static BufferedImage fromBase64(final @NonNull String base64ImageData){
        val imageData = _Bytes.ofUrlBase64.apply(base64ImageData.getBytes(StandardCharsets.UTF_8));
        return fromBytes(imageData);
    }

    // -- PIXELS

    /**
     * @return 2 dim. array of pixels defining this image,
     *      where each pixel is a 32 bit ARGB color value,
     *      with {@code A} the alpha value as highest significant 8 bits
     *      followed by {@code R} the red value and so on
     */
    @Nullable
    public static int[][] toPixels(final @Nullable BufferedImage image){

        if(image==null) {
            return null;
        }

        final int width = image.getWidth();
        final int height = image.getHeight();
        val pixels = new int[height][width];
        for(int lineIndex=0; lineIndex<height; ++lineIndex) {
            image.getRGB(0, lineIndex, width, 1, pixels[lineIndex], 0, width);

            // debug
            // System.err.println(
            //           _Ints.rowForm(pixels[lineIndex],10, Integer::toHexString));

        }
        return pixels;
    }

    /**
     * @param pixels - 2 dim. array of pixels defining this image,
     *      where each pixel is a 32 bit ARGB color value,
     *      with {@code A} the alpha value as highest significant 8 bits
     *      followed by {@code R} the red value and so on
     */
    @Nullable
    public static BufferedImage fromPixels(final @Nullable int[][] pixels){

        /*sonar-ignore-on*/
        final int height = _NullSafe.size(pixels);
        final int width = height>0
                ? _NullSafe.size(pixels[0])
                : 0;
        /*sonar-ignore-off*/

        final int pixelCount = width * height;

        if(pixelCount>0) {
            // internally clones pixel data
            val raster = createRasterARGB8888(pixels);
            return createImageARGB8888(raster);
        } else {
            return null;
        }
    }

    // -- RASTER UTILS (LOW LEVEL)

    private static WritableRaster createRasterARGB8888(final int[][] pixels){
        final int height = pixels.length;
        final int width = pixels[0].length;
        return createRasterARGB8888(width, height, _Ints.flatten(pixels));
    }

    private static WritableRaster createRasterARGB8888(final int width, final int height, final int[] dataArray){
        val dataBuffer = new DataBufferInt(dataArray, width * height);
        val sampleModel = new SinglePixelPackedSampleModel(
                dataBuffer.getDataType(), width, height, BitMask8888);
        return Raster.createWritableRaster(sampleModel, dataBuffer, null);
    }

    private static BufferedImage createImageARGB8888(final WritableRaster raster){
        val directColorModel​ =
                new DirectColorModel(32, BitMask8888[0], BitMask8888[1], BitMask8888[2], BitMask8888[3]);
        return new BufferedImage(directColorModel​, raster, false, null);
    }

    private final static int[] BitMask8888 = {
            0xff<<16,
            0xff<<8,
            0xff,
            0xff<<24,
    };

    public static int compare(final BufferedImage a, final BufferedImage b) {
        int c = Integer.compare(a.getWidth(), b.getWidth());
        if(c!=0) {
            return c;
        }
        c = Integer.compare(a.getHeight(), b.getHeight());
        if(c!=0) {
            return c;
        }
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                c = Integer.compare(a.getRGB(x, y), b.getRGB(x, y));
                if(c!=0) {
                    return c;
                }
            }
        }
        return 0;
    }

//    public static void draw(BufferedImage image, BufferedImage virtualImage) {
//        Graphics2D g2d = virtualImage.createGraphics();
//        g2d.drawImage(image, 0, 0, null);
//        g2d.dispose();
//    }


}
