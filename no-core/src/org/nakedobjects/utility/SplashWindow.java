package org.nakedobjects.utility;



import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;


public class SplashWindow extends Window implements Runnable {
    private static final String DIRECTORY = "images" + File.separator;
    private static final String LOGO_FILE = "logo.jpg";
    final static Logger LOG = Logger.getLogger(SplashWindow.class);

    /**
     * Get an Image object from the specified file path on the file system.
     */
    private static Image loadAsFile(String filename) {
        File file = new File(DIRECTORY + filename);

        if (!file.exists()) {
            LOG.error("could not find image file: " + file.getAbsolutePath());

            return null;
        } else {
            Toolkit t = Toolkit.getDefaultToolkit();
            Image image = t.getImage(file.getAbsolutePath());

            MediaTracker mt = new MediaTracker(new Canvas());
            if (image != null) {
                mt.addImage(image, 0);

                try {
                    mt.waitForAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mt.isErrorAny()) {
                    LOG.error("failed to load image from file: " + file.getAbsolutePath());
                    mt.removeImage(image);
                    image = null;
                } else {
                    mt.removeImage(image);
                    LOG.info("image loaded from file: " + file);
                }
            }

            return image;
        }
    }

    /**
     * Get an Image object from the jar/zip file that this class was loaded
     * from.
     */
    private static Image loadAsResource(String ref) {
        URL url = SplashWindow.class.getResource("/" + DIRECTORY + ref);
        LOG.debug("image from " + url);
        if (url == null) {
            return null;
        }
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        MediaTracker mt = new MediaTracker(new Canvas());
        if (image != null) {
            mt.addImage(image, 0);

            try {
                mt.waitForAll();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mt.isErrorAny()) {
                LOG.error("failed to load image from resources: " + url + " " + mt.getErrorsAny()[0]);
                mt.removeImage(image);
                image = null;
            } else {
                mt.removeImage(image);
                LOG.info("image loaded from resources: " + url);
            }
        }
        return image;
    }

    /**
     * Load a java.awt.Image object using the file name <code>name</code>.
     * This method attempts to load the image from the jar/zip file this class
     * was loaded from ie, your application, and then from the file system as a
     * file if can't be found as a resource. If neither method works the default
     * image is returned.
     * 
     * @see java.lang.Class#getResource(String)
     */
    public static Image loadImage(String name) {
        Image image = loadAsResource(name);
        if (image == null) {
            image = loadAsFile(name);
        }
        return image;
    }

    private int delay;
    private final Font labelFont;
    private final int height;
    private final int labelLineHeight;
    private Image logo;
    private final int PADDING = 9;
    private Frame parent;
    private final int width;
    private Font titleFont;

    public SplashWindow() {
        super(new Frame());
        parent = (Frame) getParent();
        logo = loadImage(LOGO_FILE);

        labelFont = new Font("SansSerif", Font.PLAIN, 9);
        titleFont = new Font("SansSerif", Font.BOLD, 24);
        FontMetrics labelMetrics = getFontMetrics(labelFont);
        FontMetrics titleMetrics = getFontMetrics(titleFont);
        labelLineHeight = (int) (labelMetrics.getHeight() * 1.05);
        
        int width;
        int height = PADDING;
        if(logo == null) {
            width = titleMetrics.stringWidth(AboutNakedObjects.getName());
            height += titleMetrics.getHeight();
        } else {
	        width = logo.getWidth(this);
	        height += logo.getHeight(this);
      }
        height += PADDING;
        
        width = Math.max(width, labelMetrics.stringWidth(AboutNakedObjects.getCopyrightNotice()));
        height += labelLineHeight;
            
        width = Math.max(width, labelMetrics.stringWidth(AboutNakedObjects.getVersion()));
        height += labelLineHeight;

        height += PADDING * 2;

        width = PADDING + width + PADDING;
        setSize(width, height);

        this.height = height;
        this.width = width;
        
        Dimension screen = getToolkit().getScreenSize();
        int x = (screen.width / 2) - (width / 2);

        if ((screen.width / screen.height) >= 2) {
            x = (screen.width / 4) - (width / 2);
        }
        
        int y = (screen.height / 2) - (width / 2);
        setLocation(x, y);
        setBackground(Color.black);

        show();
        toFront();
    }

    public void paint(Graphics g) {
        g.setColor(Color.gray);
        g.drawRect(0, 0, width - 1, height - 1);
        
        if (logo != null) {
            g.drawImage(logo, PADDING, PADDING, this);
        } else {
	        g.setFont(titleFont);
	        FontMetrics fm = g.getFontMetrics();
	        String name= AboutNakedObjects.getName();
	        g.drawString(name, PADDING, PADDING + fm.getAscent());
        }
        
        g.setFont(labelFont);
        FontMetrics fm = g.getFontMetrics();
        

        String build = AboutNakedObjects.getBuildId();
        int left3 = width / 2 - fm.stringWidth(build) / 2;
        int baseline3 = height - PADDING - fm.getDescent();        
        
        
        String copyrightNotice = AboutNakedObjects.getCopyrightNotice();
        int left1 = width / 2 - fm.stringWidth(copyrightNotice) / 2;
        int baseline2 = baseline3 - fm.getHeight();        

        String version = AboutNakedObjects.getVersion();
        int left2 = width / 2 - fm.stringWidth(version) / 2;
        int baseline1 = baseline2 - fm.getHeight();        

        int left = Math.min(left1, left2);
        left = Math.min(left, left3);
        
        g.drawString(copyrightNotice, left, baseline1);
        g.drawString(version, left, baseline2);
        g.drawString(build, left, baseline3);
    }

    /**
     * leaves the screen up for the specified period (in seconds) and then
     * removes it.
     */
    public void removeAfterDelay(int seconds) {
        this.delay = seconds * 1000;
        new Thread(this).start();
    }

    public void removeImmediately() {
        hide();
        dispose();
        parent.dispose();
    }

    public void run() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {}

        removeImmediately();
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
