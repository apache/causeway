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

package org.apache.isis.applib.value;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.internal.base._NullSafe;

/**
 * Represents an image.
 */
// tag::refguide[]
// end::refguide[]
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProvider")
public class Image implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final int[][] pixels;
    
    private final int width;
    private final int height;
    

    /**
     * @param pixels - 2 dim array of pixels defining this image, 
     *      where each pixel is a 32 bit ARGB color value,
     *      with {@code A} the alpha value as highest significant 8 bits
     *      followed by {@code R} the red value and so on
     */
    public Image(final @Nullable int[][] pixels) {
        
        final int width = _NullSafe.size(pixels);
        final int height = width>0
                ? _NullSafe.size(pixels[0])
                : 0;
        final int pixelCount = width * height;

        if(pixelCount>0) {
            this.width = width;
            this.height = height;
            this.pixels = pixels; // should make a clone, but for performance reasons we just copy the reference 
        } else {
            this.width = 0;
            this.height = 0;
            this.pixels = null;
        }
        
    }

    /**
     * @return 2 dim array of pixels defining this image, 
     *      where each pixel is a 32 bit ARGB color value,
     *      with {@literal A} the alpha value as highest significant 8 bits
     *      followed by {@code R} the red value and so on
     */
    @Nullable
    public int[][] getPixels() {
        // should return a clone, but for performance reasons we expose the internal array
        return pixels;
    }
    
    /**
     * @return height of this image or 0 if empty
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return width of this image or 0 if empty
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * 
     * @return whether this image has any pixels
     */
    public boolean isEmpty() {
        return pixels==null;
    }
    
    @Override
    public String toString() {
        return isEmpty()
                ? "Image [empty]"
                : String.format("Image [size=%dx%d]", width, height);
    }
    
}
