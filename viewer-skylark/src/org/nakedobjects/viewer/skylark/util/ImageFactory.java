package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.UiConfiguration;

import java.util.Hashtable;


public class ImageFactory {
    private static ImageFactory instance;

    public static ImageFactory getInstance() {
        if (instance == null) {
            instance = new ImageFactory();
        }
        return instance;
    }

    private TemplateImageLoader loader;

    /**
     * Keyed list of icons (each derived from an image, of a specific size etc),
     * where the key is the name of the icon and its size.
     */
    private Hashtable templateImages = new Hashtable();

    private String directory;

    private ImageFactory() {
        loader = new TemplateImageLoader();
    }

    /**
     * Loads the fallback icon image, for use when no specific image can be
     * found
     */
    public Image createFallbackIcon(final int height, final Color tint) {
        String fallbackImage = UiConfiguration.getInstance().fallbackIconImage();
        return createIcon(fallbackImage, height, tint);
    }

    /**
     * Loads an icon of the specified size, and with the specified tint. If
     * color is null then no tint is applied.
     */
    public final Image createIcon(final String name, final int height, final Color tint) {
        final String id = name + "/" + height + "/" + tint;

        if (templateImages.containsKey(id)) {
            return (Image) templateImages.get(id);

        } else {
            final String directory = directory();
            TemplateImage template = loader.getTemplateImage(directory + name);
            if (template == null) {
                return null;
            } else {
                Image icon = template.getIcon(height, tint);
                templateImages.put(id, icon);
                return icon;
            }
        }
    }

    private String directory() {
        if(directory == null) {
            directory = UiConfiguration.getInstance().imageDirectory();
        }
        return directory;
    }

    /**
     * Load a picture from the given file path.
     */
    public Image createImage(String path) {
        final String directory = directory();
        TemplateImage template = loader.getTemplateImage(directory + path);
        if (template == null) {
            return null;
        }
        return template.getFullSizeImage();
    }

    /**
     * Determines if the named icon is available.
     */
    public boolean isIconAvailable(final String name, final int height, final Color tint) {
        final String id = name + "/" + height + "/" + tint;
        if (templateImages.containsKey(id)) {
            return true;

        } else {
            String directory = directory();
            TemplateImage template = loader.getTemplateImage(directory + name);
            if (template == null) {
                return false;
            } else {
                Image icon = template.getIcon(height, tint);
                templateImages.put(id, icon);
                return true;
            }
        }
    }
    
    
    public Image loadIcon(final NakedObjectSpecification specification, final String type, int iconHeight) {
        String className = specification.getFullName().replace('.', '_') + type;
        Image loadIcon = loadIcon(className, iconHeight);
        if (loadIcon == null) {
            className = specification.getShortName();
            loadIcon = loadIcon(className, iconHeight);
            if (loadIcon == null) {
                NakedObjectSpecification superclass = specification.superclass();
                if (superclass == null) {
                    return loadUnknownIcon(iconHeight);
                }
                return loadIcon(superclass, type, iconHeight);
            }
        }

        return loadIcon;
    }

    private Image loadIcon(final String iconName, int iconHeight) {
        return ImageFactory.getInstance().createIcon(iconName, iconHeight, null);
    }

    private Image loadUnknownIcon(int iconHeight) {
        return ImageFactory.getInstance().createFallbackIcon(iconHeight, null);
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
