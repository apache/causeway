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

package org.apache.isis.core.metamodel.facets.value.image;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.exceptions.unrecoverable.UnexpectedCallException;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;

public abstract class ImageValueSemanticsProviderAbstract<T>
extends ValueSemanticsProviderAndFacetAbstract<T>
implements ImageValueFacet {

    private static final int TYPICAL_LENGTH = 18;
    private static final Object DEFAULT_VALUE = null; // no default

    private FacetHolder facetHolder;

    @SuppressWarnings("unchecked")
    public ImageValueSemanticsProviderAbstract(final FacetHolder holder, final Class<T> adaptedClass) {
        super(ImageValueFacet.class, holder, adaptedClass, TYPICAL_LENGTH, -1, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, (T) DEFAULT_VALUE);
    }

    /**
     * Returns null to indicate that this value does not parse entry strings
     */
    @Override
    public Parser<T> getParser() {
        return null;
    }

    @Override
    protected T doParse(final Object original, final String entry) {
        throw new UnexpectedCallException();
    }

    @Override
    public String titleString(final Object value) {
        return "image";
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return "image";
    }

    @Override
    public FacetHolder getFacetHolder() {
        return facetHolder;
    }

    @Override
    public void setFacetHolder(final FacetHolder facetHolder) {
        this.facetHolder = facetHolder;
    }

    // -- toString

    @Override
    public String toString() {
        return "ImageValueSemanticsProvider";
    }

    // -- DEPRECATED

    //  protected abstract T setPixels(int[][] pixels);
    //  public void setMask(final String mask) {}
    //  public String getIconName() {return null;}

//    @Override
//    protected String doEncode(final Object object) {
//        final int[][] image = getPixels(object);
//        final int lines = image.length;
//        final int width = image[0].length;
//        final StringBuffer encodeData = new StringBuffer(lines * width * 4);
//        encodePixel(lines, encodeData);
//        encodePixel(width, encodeData);
//        for (int line = 0; line < lines; line++) {
//            for (int pixel = 0; pixel < width; pixel++) {
//                encodePixel(image[line][pixel], encodeData);
//            }
//        }
//        return encodeData.toString();
//    }


//    @Override
//    public T doRestore(final String data) {
//        final int lines = decodePixel(data, 0);
//        final int width = decodePixel(data, 1);
//        final int[][] imageData = new int[lines][width];
//        int offset = 2;
//        for (int line = 0; line < lines; line++) {
//            for (int pixel = 0; pixel < width; pixel++) {
//                imageData[line][pixel] = decodePixel(data, offset++);
//                imageData[line][pixel] = imageData[line][pixel] | 0xFF000000;
//            }
//        }
//        final T image = setPixels(imageData);
//        return image;
//    }


//  public T restoreFromByteArray(final byte[] byteArray) {
//  final int[] flatIntArray = new int[byteArray.length / 4];
//  for (int i = 0; i < flatIntArray.length; i++) {
//      flatIntArray[i] = byteArrayToInt(byteArray, i * 4);
//  }
//  return setPixels(inflate(flatIntArray));
//}
//
//private int[] flatten(final ManagedObject object) {
//  final int[][] image = getPixels(object);
//  final int[] flatArray = new int[(getHeight(object) * getWidth(object)) + 2];
//  int flatIndex = 0;
//  flatArray[flatIndex++] = getHeight(object);
//  flatArray[flatIndex++] = getWidth(object);
//  for (int i = 0; i < getHeight(object); i++) {
//      for (int j = 0; j < getWidth(object); j++) {
//          flatArray[flatIndex++] = image[i][j];
//      }
//  }
//  return flatArray;
//}
//
//private static int[][] inflate(final int[] flatIntArray) {
//  int flatIndex = 0;
//  final int height = flatIntArray[flatIndex++];
//  final int width = flatIntArray[flatIndex++];
//
//  final int[][] newImage = new int[height][width];
//
//  for (int i = 0; i < height; i++) {
//      for (int j = 0; j < width; j++) {
//          newImage[i][j] = flatIntArray[flatIndex++];
//      }
//  }
//  return newImage;
//}

//  protected BufferedImage createImage(final int[][] pixels) {
//  final int lines = pixels.length;
//  final int width = pixels[0].length;
//  final int[] pix = new int[lines * width];
//  int offset = 0;
//  for (int line = 0; line < lines; line++) {
//      System.arraycopy(pixels[line], 0, pix, offset, width);
//      offset += width;
//  }
//  final MemoryImageSource source = new MemoryImageSource(width, lines, pix, 0, width);
//  return java.awt.Toolkit.getDefaultToolkit().createImage(source);
//}
//
//private int decodePixel(final String data, final int item) {
//  final int offset = item * 4;
//  int pixel = 0;
//  for (int i = 0; i < 4; i++) {
//      final char c = data.charAt(offset + i);
//      final int b = (REVERSE_BASE_64_CHARS[c] & 0xff);
//      if (i == 0 && b >= 32) {
//          pixel = 0xff;
//      }
//      pixel = (pixel << 6) + b;
//  }
//  return pixel;
//}

//  public byte[] getAsByteArray(final ManagedObject object) {
//  final int[] flatIntArray = flatten(object);
//  final byte[] byteArray = new byte[flatIntArray.length * 4];
//  for (int i = 0; i < flatIntArray.length; i++) {
//      final int value = flatIntArray[i];
//      byteArray[i * 4] = (byte) ((value >> 24) & 0xff);
//      byteArray[i * 4 + 1] = (byte) ((value >> 16) & 0xff);
//      byteArray[i * 4 + 2] = (byte) ((value >> 8) & 0xff);
//      byteArray[i * 4 + 3] = (byte) (value & 0xff);
//  }
//  return byteArray;
//}
//
//private static int byteArrayToInt(final byte[] byteArray, final int offset) {
//  int value = 0;
//  for (int i = 0; i < 4; i++) {
//      final int shift = (4 - 1 - i) * 8;
//      value += (byteArray[i + offset] & 0x000000FF) << shift;
//  }
//  return value;
//}

//  protected abstract int[][] getPixels(Object object);
//
//  protected int[][] grabPixels(final Image image) {
//      final int width = image.getWidth(null);
//      final int lines = image.getHeight(null);
//      final int pixels[] = new int[width * lines];
//      final PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, lines, pixels, 0, width);
//      grabber.setColorModel(ColorModel.getRGBdefault());
//      try {
//          if (grabber.grabPixels() && (grabber.status() & ImageObserver.ALLBITS) != 0) {
//              final int[][] array = new int[lines][width];
//              int srcPos = 0;
//              for (int line = 0; line < lines; line++) {
//                  array[line] = new int[width];
//                  System.arraycopy(pixels, srcPos, array[line], 0, width);
//                  for (int pixel = 0; pixel < array[line].length; pixel++) {
//                      array[line][pixel] = array[line][pixel] | 0xFF000000;
//                  }
//                  srcPos += width;
//              }
//              return array;
//          }
//          // TODO what happens if the grab fails?
//          return new int[lines][width];
//      } catch (final InterruptedException e) {
//          // Restore interrupted state...
//          Thread.currentThread().interrupt();
//          throw new IsisException(e);
//      }
//  }

//  private void encodePixel(final int pixel, final StringBuffer encodeData) {
//  // TODO the encoding is poor as the negative numbers are not dealt with
//  // properly because the 6 MSBs
//  // are not included.
//  if (pixel > 0x7FFFFF || pixel < -0x7FFFFF) {
//      // throw new IsisException("" + pixel);
//  }
//  for (int i = 3; i >= 0; i--) {
//      final int bitsToShift = i * 6;
//      final int c = pixel >> bitsToShift;
//      final char cc = BASE_64_CHARS[c & 0x3F];
//      encodeData.append(cc);
//  }
//}


}
