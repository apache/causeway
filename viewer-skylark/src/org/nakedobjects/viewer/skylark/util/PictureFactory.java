package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Picture;
import org.nakedobjects.viewer.skylark.UiConfiguration;

import java.util.Hashtable;


public class PictureFactory {
    private static PictureFactory instance;

    public static PictureFactory getInstance() {
        if (instance == null) {
            instance = new PictureFactory();
        }
        return instance;
    }

    /**
     * Keyed list of icons (each derived from an image, of a specific size etc),
     * where the key is the name of the icon and its size.
     */
    private Hashtable pictures = new Hashtable();
    private PictureLoader loader;

    private PictureFactory() {
        String fallbackImage = UiConfiguration.getInstance().fallbackIconImage();
        String directory = UiConfiguration.getInstance().imageDirectory();
        loader = new PictureLoader(directory + fallbackImage);
    }

    /**
     * Determines if the named icons is available.
     */
    public boolean isIconAvailable(final String name, final int height, final Color tint) {
        final String id = name + "/" + height + "/" + tint;
        if (pictures.containsKey(id)) {
            return true;

        } else {
            String directory = UiConfiguration.getInstance().imageDirectory();
            PictureTemplate template = loader.getPictureTemplate(directory + name);
            if (template == null) {
                return false;
            } else {
                Picture icon = template.getIcon(height, tint);
                pictures.put(id, icon);
                return true;
            }
        }
    }

    /**
     * Loads an icon of the specified size, and with the specified tint.  If color is null then no 
     * tint is applied.
     */
    public final Picture loadIcon(final String name, final int height, final Color tint) {
        final String id = name + "/" + height + "/" + tint;

        if (pictures.containsKey(id)) {
            return (Picture) pictures.get(id);

        } else {
            final String directory = UiConfiguration.getInstance().imageDirectory();
            PictureTemplate template = loader.getPictureTemplate(directory + name);
            if(template == null) {
                return null;            
            } else {
                Picture icon = template.getIcon(height, tint);
                pictures.put(id, icon);
                return icon;
            }
        }
    }

    /**
     * Load a picture from the given file path.
      */
    public Picture loadPicture(String path) {
        final String directory = UiConfiguration.getInstance().imageDirectory();
        PictureTemplate template = loader.getPictureTemplate(directory + path);
        return template.getFullSizeImage();
    }

    /**
     * Loads the fallback icon image, for use when no specific image can be found
     */
    public Picture loadUnknownIcon(final int height, final Color tint) {
        final String id = "unknown-icon/" + height + "/" + tint;

        if (pictures.containsKey(id)) {
            return (Picture) pictures.get(id);

        } else {
            PictureTemplate template = loader.getUnknowIconPictureTemplate();
            Picture icon = template.getIcon(height, tint);
            pictures.put(id, icon);
            return icon;
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
