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

package org.apache.isis.viewer.html.image;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * ImageLookup provides an efficient way of finding the most suitable image to
 * use.
 * 
 * <p>
 * It ensures that an image is always available, providing a default image if
 * needed. All requests are cached to improve performance.
 */
// TODO allow for multiple extension types
public class ImageLookup {

    private static ImageProvider imageProvider = new ImageProviderResourceBased();

    public static ImageProvider getInstance() {
        return imageProvider;
    }

    public static void setImageDirectory(final String imageDirectory) {
        if (getInstance() instanceof ImageProviderDirectoryBased) {
            final ImageProviderDirectoryBased imageProviderDirectoryBased = (ImageProviderDirectoryBased) imageProvider;
            imageProviderDirectoryBased.setImageDirectory(imageDirectory);
        }
    }

    public static void debug(final DebugBuilder debug) {
        getInstance().debug(debug);
    }

    public static String image(final ObjectAdapter object) {
        return getInstance().image(object);
    }

    public static String image(final ObjectSpecification specification) {
        return getInstance().image(specification);
    }

    public static String image(final String name) {
        return getInstance().image(name);
    }

}
