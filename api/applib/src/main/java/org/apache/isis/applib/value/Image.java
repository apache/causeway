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

import org.apache.isis.applib.annotation.Value;

/**
 * Represents an image.
 */
// tag::refguide[]
// end::refguide[]
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProvider")
public class Image implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int[][] pixels;

    /**
     * 
     * @param pixels - 2 dim array of pixels defining this image, where each pixel is a 32 bit ARGB color value;
     */
    public Image(final int[][] pixels) {
        this.pixels = pixels;
    }

    public Object getValue() {
        return pixels;
    }

    @Override
    public String toString() {
        final int height = getHeight();
        return "Image [size=" + height + "x" + (height == 0 || pixels[0] == null ? 0 : pixels[0].length) + "]";
    }

    /**
     * @return 2 dim array of pixels defining this image, where each pixel is a 32 bit ARGB color value
     */
    public int[][] getPixels() {
        return pixels;
    }
    
    public int getHeight() {
        return pixels == null ? 0 : pixels.length;
    }

    public int getWidth() {
        return pixels == null ? 0 : pixels[0].length;
    }
}
