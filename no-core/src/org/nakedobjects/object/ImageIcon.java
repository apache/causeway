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

package org.nakedobjects.object;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import org.apache.log4j.Category;
import org.nakedobjects.utility.Configuration;


/**
   A indexed cache for Image objects.
   Images are retrieved from as resources, from the jar or zip file, or directly off the disk.  A default image
   is kept in memory in case the forementioned methods fail to find a specified image.  The default image
   is unknown.gif and will normally be found in the toolkit file unless it has been specifically built
   without it (that is the toolkit is supplied with a default image inside the jar file).
 */
public class ImageIcon {
    final static Category LOG = Category.getInstance(ImageIcon.class);
    private static final String DIRECTORY = "images" + File.separator;
	private static boolean alsoLoadAsFiles;

    /**
       For an image that is normal sized (32 x 32 pixels) this constant should be passed to getImageIcon()
       the ImageIcon constructors.
     */
    public static final int NORMAL = 32;

    /**
       For an image that is small (16 x 16 pixels) this constant should be passed to getImageIcon()
       the ImageIcon constructors.
     */
    public static final int SMALL = 16;

    // CACHE	
    private static Hashtable imageIcons = new Hashtable();
    private static MediaTracker mt = new MediaTracker(new Canvas());
    private static final Image largeUnknown;
    private static final Image smallUnknown;
    private Image image;

    // Image
    private String name;
    private String status;
    private int size;
    
    static {
		alsoLoadAsFiles = Configuration.getInstance().getBoolean("viewer.lightweight.load-images-from-files", true);
		Image im = loadAsResource("UnknownLarge.gif");
    	if(im == null) {
    		Component c = new Canvas();
    		largeUnknown = c.createImage(NORMAL, NORMAL);
    	} else {
    		largeUnknown = im;
    	}
    }
    
	static {    	
		Image im = loadAsResource("UnknownSmall.gif");
		if(im == null) {
			Component c = new Canvas();
			smallUnknown = c.createImage(NORMAL, NORMAL);
		} else {
			smallUnknown = im;
		}
    }

    /**
       Constructor for building an ImageIcon.  An Image, which will be the Image passed back to clients in
       the future, and name, size and status, which together give the ImageIcon a unique reference are used
       to create an ImageIcon.  This constructor is normally called by <code>getImageIcon()</code> but can be
       called directly if an Image icon needs to be created in an indirect manner. Ie, the image to be used is already
       loaded - eg, after filtering - or the image to be used does not want to use the concatonated name, size and
       status as it reference.
     */
    public ImageIcon(Image image, String name, int size, String status) {
        this.image = image;
        this.name = name;
        this.size = size;
        this.status = status;
        imageIcons.put(name + size + status, this);
    }

    public static String getDirectory() {
        return DIRECTORY;
    }

    /**
       Returns the java.awt.Image object that this object is holding.
     */
    public Image getImage() {
        if (image == null) {
            throw new NullPointerException();
        } else {
            return image;
        }
    }

    public static Image getImageIcon(String name, int size) {
    	return loadImage(name, size, "");
    }


    /**
       Gets an ImageIcon object with the specified details.  If the image is
       not cached the image is loaded using loadImage() with the parameters <code>name</code>,
       <code>size</code> and <code>status</code> being concatenated with ".gif" to form a file name.
       Eg, where name is "customer", size is SMALL and status is "" the image sought will be customer16.gif; if
       the name order, size is NORMAL and status is "fulfilled" then the reference would be order32fulfilled.gif.
       @return the corresponding ImageIcon or null if no image found.
     */
    public static ImageIcon getImageIcon(String name, int size, String status) {
        String ref = name + size + status;

        if (imageIcons.containsKey(ref)) {
            return (ImageIcon) imageIcons.get(ref);
        } else {
            Image image = loadImage(name, size, status);
            ImageIcon icon = new ImageIcon(image, name, size, status);

            return icon;
        }
    }

    /**
       The name of this image.  This name is not unique as it is shared by all the ImageIcons that
       also have this name but differ in size and status.
     */
    public String getName() {
        return name;
    }

    /**
       Load a java.awt.Image object using the file name <code>name</code>.  This method attempts to
       load the image from the jar/zip file this class was loaded from ie, your application, and then from
       the file system as a file if can't be found as a resource.
       If neither method works the default image is returned.
       @see java.lang.Class#getResource(String)
     */
    public static Image loadImage(String name) {
        Image image;

        image = loadAsResource(name);

        if (image == null && alsoLoadAsFiles) {
            image = loadAsFile(name);
        }

        if (image == null) {
            if (name.indexOf("" + SMALL) > 0) {
                image = smallUnknown;
            } else if (name.indexOf("" + NORMAL) > 0) {
                image = largeUnknown;
            }
        }

        if (image == null) {
            LOG.error("No image loaded for: " + name);
        }

        return image;
    }

    	/**
       Load a java.awt.Image object using the file name <code>name</code>.  This method attempts to
       load the image from the jar/zip file this class was loaded from ie, your application, and then from
       the file system as a file if can't be found as a resource.
       If neither method works the default image is returned.
       @see java.lang.Class#getResource(String)
     */
    public static Image loadImage(String name, int size, String status) {
        String ref;

        if (size == 0) {
            ref = name + status + ".gif";
        } else {
            ref = name + size + status + ".gif";
        }

        return loadImage(ref);
    }

    /**
       Sets Image object that this object is holding.
     */
    public void setImage(Image image) {
        this.image = image;
    }

    public String toString() {
        return "ImageIcon [name=" + name + ",size=" + size + ",size=" + status + ",image=" + image +
        "]";
    }

    /**
       Get an Image object from the specified file path on the file system.
     */
    private static Image loadAsFile(String filename) {
        File file = new File(DIRECTORY + filename);

        if (!file.exists()) {
            LOG.error("Could not find image file: " + file.getAbsolutePath());

            return null;
        } else {
            Toolkit t = Toolkit.getDefaultToolkit();
            Image image = t.getImage(file.getAbsolutePath());

            if (image != null) {
                mt.addImage(image, 0);

                try {
                    mt.waitForAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mt.isErrorAny()) {
                    LOG.error("Failed to load image from file: " + file.getAbsolutePath());
                    mt.removeImage(image);
                    image = null;
                } else {
                    mt.removeImage(image);
                    LOG.info("Image loaded from file: " + file);
                }
            }

            return image;
        }
    }

    /**
       Get an Image object from the jar/zip file that this class was loaded from.
     */
    private static Image loadAsResource(String ref) {
        URL url = ImageIcon.class.getResource("/" + DIRECTORY + ref);

        LOG.debug("Image from " + url);

        if (url == null) {
            return null;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(url);

        if (image != null) {
            mt.addImage(image, 0);

            try {
                mt.waitForAll();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mt.isErrorAny()) {
                LOG.error("Failed to load image from resources: " + url + " " +
                    mt.getErrorsAny()[0]);
                mt.removeImage(image);
                image = null;
            } else {
                mt.removeImage(image);
                LOG.info("Image loaded from resources: " + url);
            }
        }

        return image;
    }
}
