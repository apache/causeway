/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.configuration.Configuration;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Icon;
import org.nakedobjects.viewer.skylark.Viewer;

import java.util.Hashtable;


public class ImageFactory {
	private final static String FALLBACK_PARAM = Viewer.PROPERTY_BASE + "fallback-image";
	private final static String DIRECTORY = "images/";
	private static ImageFactory instance;
	private ImageLoader loader;

    /**
     * Keyed list of icons (each derived from an image, of a specific size etc), where the
     * key is the name of the icon and its size.
     */
    private Hashtable icons = new Hashtable();

	public static ImageFactory getImageFactory() {
		if(instance == null ) {
			instance = new ImageFactory();
		}
		return instance;
	}
	
	private ImageFactory() {
		String fallbackImage = Configuration.getInstance().getString(FALLBACK_PARAM, "Unknown.gif");
		loader = new ImageLoader(DIRECTORY + fallbackImage);
	}

	
	/**
	   Creates a flyweight Icon object of the specified size.
	   @return the corresponding ImageIcon or null if no image found.
	 */
   public final Icon createIcon(String name, int height, Color tint) {
		String id = name + "/" + height + "/" + tint;

		if (icons.containsKey(id)) {
			return (Icon) icons.get(id);
			
		} else {
			ImageTemplate template = loader.getImageTemplateOrFallback(DIRECTORY + name);
			Icon icon = template.getIcon(height, tint);
			icons.put(id, icon);

			return icon;
		}
	}

		/**
	   Load a java.awt.Image object using the file path <code>path</code>.  This method attempts to
	   load the image from the jar/zip file this class was loaded from ie, your application, and then from
	   the file system as a file if can't be found as a resource.
	   If neither method works the default image is returned.
	   @see java.lang.Class#getResource(String)
	 */
	public Icon loadImage(String path) {
		ImageTemplate template = loader.getImageTemplateOrFallback(DIRECTORY + path);
		return template.getFullSizeImage();
	}

    public boolean imageAvailable(String name, int height, Color tint) {
		String id = name + "/" + height + "/" + tint;

		if (icons.containsKey(id)) {
			return true;
			
		} else {
			ImageTemplate template = loader.getImageTemplate(DIRECTORY + name);
			if(template == null) {
			    return false;
			} else {
				Icon icon = template.getIcon(height, tint);
				icons.put(id, icon);
				return true;
			}
		}
}
}

