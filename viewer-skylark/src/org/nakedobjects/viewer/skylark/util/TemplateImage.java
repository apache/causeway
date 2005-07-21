package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.core.AwtImage;

import java.awt.Canvas;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

import org.apache.log4j.Logger;


/**
 * Many icons are based on the same image, but in different sizes and possibly
 * different colours. The ImageTemplate class loads and holds them image, and
 * can provide it clients with the full sized images or scaled images.
 */
class TemplateImage {

    private class Filter extends RGBImageFilter {

        public int filterRGB(int x, int y, int rgb) {
            return 0xFFFFFF - rgb;
        }
    }

    private final static Logger LOG = Logger.getLogger(TemplateImage.class);

    static TemplateImage create(java.awt.Image image) {
        if (image == null) {
            return null;
        }
        return new TemplateImage(image);

    }

    private java.awt.Image image;
    private MediaTracker mt = new MediaTracker(new Canvas());

    private TemplateImage(java.awt.Image image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
    }

    public Image getFullSizeImage() {
        return new AwtImage(image);
    }

    public Image getIcon(int height, Color tint) {
        java.awt.Image iconImage;

        if (height == image.getHeight(null)) {
            iconImage = image;
        } else {
            iconImage = image.getScaledInstance(-1, height, java.awt.Image.SCALE_SMOOTH);

            if (iconImage != null) {
                mt.addImage(iconImage, 0);

                try {
                    mt.waitForAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mt.isErrorAny()) {
                    LOG.error("Failed to create scaled image: " + iconImage + " " + mt.getErrorsAny()[0]);
                    mt.removeImage(iconImage);
                    iconImage = null;
                } else {
                    mt.removeImage(iconImage);
                    LOG.info("Image " + iconImage + " scaled to " + height);
                }
            }

            if (iconImage == null || iconImage.getWidth(null) == -1) {
                throw new RuntimeException("scaled image! " + iconImage.toString());
            }
        }

        if (tint != null) {
            LOG.debug("tinting image " + tint);
            Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(iconImage.getSource(), new Filter()));
        }

        return new AwtImage(iconImage);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
