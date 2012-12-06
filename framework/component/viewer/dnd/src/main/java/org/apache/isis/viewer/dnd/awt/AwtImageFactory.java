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

package org.apache.isis.viewer.dnd.awt;

import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

import org.apache.isis.core.runtime.imageloader.TemplateImage;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.view.base.AwtImage;

public class AwtImageFactory extends ImageFactory {

    private static class Filter extends RGBImageFilter {
        @Override
        public int filterRGB(final int x, final int y, final int rgb) {
            return 0xFFFFFF - rgb;
        }
    }

    private final TemplateImageLoader loader;

    public AwtImageFactory(final TemplateImageLoader imageLoader) {
        loader = imageLoader;
    }

    /**
     * Load an image with the given name.
     */
    @Override
    public Image loadImage(final String path) {
        final TemplateImage template = templateImage(path);
        if (template == null) {
            return null;
        }
        return new AwtImage(template.getImage());
    }

    @Override
    protected Image loadImage(final String name, final int height, final Color tint) {
        final TemplateImage template = templateImage(name);
        if (template == null) {
            return null;
        }
        final java.awt.Image iconImage = template.getIcon(height);
        if (tint != null) {
            Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(iconImage.getSource(), new Filter()));
        }
        final Image icon = new AwtImage(iconImage);
        return icon;
    }

    // ////////////////////////////////////////////////////////////////////
    // Helpers
    // ////////////////////////////////////////////////////////////////////

    private TemplateImage templateImage(final String name) {
        final TemplateImage template = loader.getTemplateImage(name);
        return template;
    }

}
