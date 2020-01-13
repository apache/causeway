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
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProvider")
public class Image implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int[][] image;

    public Image(final int[][] image) {
        this.image = image;
    }

    public Object getValue() {
        return image;
    }

    @Override
    public String toString() {
        final int height = getHeight();
        return "Image [size=" + height + "x" + (height == 0 || image[0] == null ? 0 : image[0].length) + "]";
    }

    public int[][] getImage() {
        return image;
    }

    public int getHeight() {
        return image == null ? 0 : image.length;
    }

    public int getWidth() {
        return image == null ? 0 : image[0].length;
    }
}
