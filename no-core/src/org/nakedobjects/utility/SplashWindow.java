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
    private static final String IMAGE_DIRECTORY = "images";
    private static final String LOGO_FILE = "logo.jpg";
    final static Logger LOG = Logger.getLogger(SplashWindow.class);
    private static final String LOGO_TEXT = "Naked Objects";

    /**
     * Get an Image object from the specified file path on the file system.
     */
    private static Image loadAsFile(String filename) {
        File file = new File(IMAGE_DIRECTORY + File.separator + filename);
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
     * Get an Image object from the jar/zip file that this class was loaded from.
     */
    private static Image loadAsResource(String ref) {
        URL url = SplashWindow.class.getResource("/" + IMAGE_DIRECTORY + "/"+ ref);
        if (url == null) {
            LOG.debug("image not found for resource named " + ref);
            return null;
        }
        LOG.debug("image available from resource " + url);
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
     * Load a java.awt.Image object using the file name <code>name</code>. This method attempts to load the
     * image from the jar/zip file this class was loaded from ie, your application, and then from the file
     * system as a file if can't be found as a resource. If neither method works the default image is
     * returned.
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
    private final Font textFont;
    private final int height;
    private final int textLineHeight;
    private final int titleLineHeight;
    private Image logo;
    private final int PADDING = 9;
    private Frame parent;
    private final int width;
    private Font titleFont;
    private int left;
    private Font logoFont;

    public SplashWindow() {
        super(new Frame());
        parent = (Frame) getParent();
        logo = loadImage(LOGO_FILE);

        textFont = new Font("SansSerif", Font.PLAIN, 10);
        titleFont = new Font("SansSerif", Font.BOLD, 11);
        logoFont = new Font("Serif", Font.PLAIN, 36);
        textLineHeight = (int) (getFontMetrics(textFont).getHeight() * 0.85);
        titleLineHeight = (int) (getFontMetrics(titleFont).getHeight() * 1.20);

        int height = 0;
        int width = 0;
        
        if (logo != null) {
            width = logo.getWidth(this);
            height += logo.getHeight(this);
        } else {
            FontMetrics metrics = getFontMetrics(logoFont);
            width = metrics.stringWidth(LOGO_TEXT);
            height = metrics.getHeight();
        }
        height += PADDING;

        Dimension text = textBounds();
        width = Math.max(width, text.width);
        height += text.height;
        
        height = PADDING + height + PADDING;
        width = PADDING + width + PADDING;
        setSize(width, height);

        this.height = height;
        this.width = width;
        this.left = width / 2 - text.width / 2;

        setupCenterLocation();

        show();
        toFront();
    }

    private void setupCenterLocation() {
        Dimension screen = getToolkit().getScreenSize();
        int x = (screen.width / 2) - (this.width / 2);
        if ((screen.width / screen.height) >= 2) {
            x = (screen.width / 4) - (this.width / 2);
        }
        int y = (screen.height / 2) - (this.width / 2);
        setLocation(x, y);
        setBackground(Color.black);
    }

    private Dimension textBounds() {
        FontMetrics textMetrics = getFontMetrics(textFont);
        FontMetrics titleMetrics = getFontMetrics(titleFont);
        int width = 0;
        int height = 0;

        
        // framework details
        width = titleMetrics.stringWidth(AboutNakedObjects.getFrameworkName());
        height += titleLineHeight;
        width = Math.max(width, textMetrics.stringWidth(AboutNakedObjects.getFrameworkCopyrightNotice()));
        height += textLineHeight;
        width = Math.max(width, textMetrics.stringWidth(frameworkVersion()));
        height += textLineHeight;

        // application details
        String text = AboutNakedObjects.getApplicationName();
        if (text != null) {
            width = Math.max(width, titleMetrics.stringWidth(text));
            height += titleLineHeight;
        }
        text = AboutNakedObjects.getApplicationCopyrightNotice();
        if (text != null) {
            width = Math.max(width, textMetrics.stringWidth(text));
            height += textLineHeight;
        }
        text = AboutNakedObjects.getApplicationVersion();
        if (text != null) {
            width = Math.max(width, textMetrics.stringWidth(text));
            height += textLineHeight;
        }

        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.setColor(Color.gray);
        g.drawRect(0, 0, width - 1, height - 1);

        if (logo != null) {
            g.drawImage(logo, PADDING, PADDING, this);
     //       g.drawRect(PADDING, PADDING, logo.getWidth(this) - 1, logo.getHeight(this) - 1);
        } else {
            g.setFont(logoFont);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(LOGO_TEXT, PADDING, PADDING + fm.getAscent());
        }

        int baseline = height - PADDING - getFontMetrics(textFont).getDescent();
        
        // framework details - from bottom to top
        g.setFont(textFont);
        g.drawString(frameworkVersion(), left, baseline);
        baseline -= textLineHeight;
        g.drawString(AboutNakedObjects.getFrameworkCopyrightNotice(), left, baseline);
        baseline -= textLineHeight;
        g.setFont(titleFont);
        g.drawString(AboutNakedObjects.getFrameworkName(), left, baseline);
        baseline -= titleLineHeight;
        
        // application details - from bottom to top
        g.setFont(textFont);
        String applicationVersion = AboutNakedObjects.getApplicationVersion();
        if (applicationVersion != null) {
            g.drawString(applicationVersion, left, baseline);
            baseline -= textLineHeight;
        }
        String applicationCopyrightNotice = AboutNakedObjects.getApplicationCopyrightNotice();
        if (applicationCopyrightNotice != null) {
            g.drawString(applicationCopyrightNotice, left, baseline);
            baseline -= textLineHeight;
        }
        String applicationName = AboutNakedObjects.getApplicationName();
        if (applicationName != null) {
            g.setFont(titleFont);
            g.drawString(applicationName, left, baseline);
        }
    }

    private String frameworkVersion() {
        return AboutNakedObjects.getFrameworkVersion() + " (" + AboutNakedObjects.getFrameworkBuild() + ")";
    }

    /**
     * leaves the screen up for the specified period (in seconds) and then removes it.
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
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
