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

package org.apache.isis.core.runtime.imageloader;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.MediaTracker;

import org.apache.log4j.Logger;

/**
 * Many icons are based on the same image, but in different sizes and possibly
 * different colours. The ImageTemplate class loads and holds them image, and
 * can provide it clients with the full sized images or scaled images.
 */
public class TemplateImageImpl implements TemplateImage {
    private final static Logger LOG = Logger.getLogger(TemplateImageImpl.class);

    /**
     * Factory method.
     */
    public static TemplateImageImpl create(final java.awt.Image image) {
        if (image == null) {
            return null;
        }
        return new TemplateImageImpl(image);

    }

    private final Image image;
    private final MediaTracker mt = new MediaTracker(new Canvas());

    private TemplateImageImpl(final Image image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public Image getIcon(final int height) {
        Image iconImage;

        if (height == image.getHeight(null)) {
            return image;
        }

        iconImage = image.getScaledInstance(-1, height, java.awt.Image.SCALE_SMOOTH);

        if (iconImage != null) {
            mt.addImage(iconImage, 0);

            try {
                mt.waitForAll();
            } catch (final Exception e) {
                // TODO: tidy up this horrid code.
                e.printStackTrace();
            }

            if (mt.isErrorAny()) {
                LOG.error("failed to create scaled image: " + iconImage + " " + mt.getErrorsAny()[0]);
                mt.removeImage(iconImage);
                iconImage = null;
            } else {
                mt.removeImage(iconImage);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("image " + iconImage + " scaled to " + height);
                }
            }
        }

        if (iconImage == null) {
            throw new RuntimeException("no icon image!");
        } else {
            if (iconImage.getWidth(null) == -1) {
                throw new RuntimeException("scaled image! " + iconImage.toString());
            }
        }

        return iconImage;
    }
}
