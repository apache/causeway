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

package org.apache.isis.viewer.dnd.drawing;

/*
 import java.awt.Toolkit;
 import java.awt.image.FilteredImageSource;
 import java.awt.image.RGBImageFilter;
 */
import java.util.Hashtable;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.util.Properties;

public abstract class ImageFactory {
    private static final String DEFAULT_IMAGE_NAME = "Default";
    private static final String DEFAULT_IMAGE_PROPERTY = Properties.PROPERTY_BASE + "default-image";
    private static ImageFactory instance;
    private static final String SEPARATOR = "_";

    public static ImageFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance not set up yet");
        }
        return instance;
    }

    /**
     * Keyed list of icons (each derived from an image, of a specific size etc),
     * where the key is the name of the icon and its size.
     */
    private final Hashtable<String, Image> templateImages = new Hashtable<String, Image>();

    // ////////////////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////////////////

    public ImageFactory() {
        instance = this;
    }

    // ////////////////////////////////////////////////////////////////////
    // loadIcon for Specifications
    // ////////////////////////////////////////////////////////////////////

    public Image loadIcon(final ObjectSpecification specification, final int iconHeight, final Color tint) {
        return findIcon(specification, iconHeight, null);
    }

    private Image findIcon(final ObjectSpecification specification, final int iconHeight, final Color tint) {
        Image loadIcon = null;
        if (loadIcon == null) {
            final String fullClassNameSlashes = specification.getFullIdentifier().replace(".", "/");
            loadIcon = loadIcon(fullClassNameSlashes, iconHeight, tint);
        }
        if (loadIcon == null) {
            final String fullClassNameUnderscores = specification.getFullIdentifier().replace('.', '_');
            loadIcon = loadIcon(fullClassNameUnderscores, iconHeight, tint);
        }
        if (loadIcon == null) {
            final String shortClassNameUnderscores = specification.getShortIdentifier().replace('.', '_');
            loadIcon = loadIcon(shortClassNameUnderscores, iconHeight, tint);
        }
        if (loadIcon == null) {
            loadIcon = findIconForSuperClass(specification, iconHeight, tint);
        }
        if (loadIcon == null) {
            loadIcon = findIconForInterfaces(specification, iconHeight, tint);
        }
        return loadIcon;
    }

    private Image findIconForSuperClass(final ObjectSpecification specification, final int iconHeight, final Color tint) {
        final ObjectSpecification superclassSpecification = specification.superclass();
        Image loadIcon;
        if (superclassSpecification == null) {
            loadIcon = null;
        } else {
            loadIcon = findIcon(superclassSpecification, iconHeight, tint);
        }
        return loadIcon;
    }

    private Image findIconForInterfaces(final ObjectSpecification specification, final int iconHeight, final Color tint) {
        Image loadIcon = null;
        for (final ObjectSpecification interfaceSpecification : specification.interfaces()) {
            loadIcon = findIcon(interfaceSpecification, iconHeight, tint);
            if (loadIcon != null) {
                return loadIcon;
            }
        }
        return loadIcon;
    }

    // ////////////////////////////////////////////////////////////////////
    // loadIcon for arbitrary path
    // ////////////////////////////////////////////////////////////////////

    /**
     * Loads an icon of the specified size, and with the specified tint. If
     * color is null then no tint is applied.
     */
    public Image loadIcon(final String name, final int height, final Color tint) {
        final String id = name + SEPARATOR + height + SEPARATOR + tint;

        if (templateImages.containsKey(id)) {
            return templateImages.get(id);
        }
        final Image icon = loadImage(name, height, tint);
        if (icon != null) {
            templateImages.put(id, icon);
        }
        return icon;
    }

    // ////////////////////////////////////////////////////////////////////
    // loadDefaultIcon
    // ////////////////////////////////////////////////////////////////////

    /**
     * Loads the fall back icon image, for use when no specific image can be
     * found
     */
    public Image loadDefaultIcon(final int height, final Color tint) {
        final String fallbackImage = getConfiguration().getString(DEFAULT_IMAGE_PROPERTY, DEFAULT_IMAGE_NAME);
        Image icon = loadIcon(fallbackImage, height, tint);
        if (icon == null) {
            icon = loadIcon("unknown", height, tint);
        }
        if (icon == null) {
            throw new IsisException("Failed to find default icon: " + fallbackImage);
        }
        return icon;
    }

    // ////////////////////////////////////////////////////////////////////
    // loadImage
    // ////////////////////////////////////////////////////////////////////

    /**
     * Load an image with the given name.
     */
    public abstract Image loadImage(final String path);

    /**
     * Load an image with the given name, but of a specific height and of a
     * specific hint.
     */
    protected abstract Image loadImage(final String name, final int height, final Color tint);

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    // ////////////////////////////////////////////////////////////////////

    private IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

}
