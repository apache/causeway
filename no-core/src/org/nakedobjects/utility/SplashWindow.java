package org.nakedobjects.utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;

import org.nakedobjects.AboutNakedObjects;
import org.nakedobjects.object.ImageIcon;


public class SplashWindow extends Window implements Runnable {
    private final int ascent;
    private int delay;
    private final Font font;
    private final int height;
    private final int lineHeight;
    private Image logo;
    private final int PADDING = 7;
    private Frame parent;
    private final int width;

    public SplashWindow() {
        super(new Frame());
        parent = (Frame) getParent();
        logo = ImageIcon.loadImage("logo.jpg");

        font = new Font("SansSerif", Font.PLAIN, 9);
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        lineHeight = (int) (fm.getHeight() * 1.05);
        ascent = fm.getAscent();

        int maxWidth = logo.getWidth(this);
        maxWidth = Math.max(maxWidth, fm.stringWidth(AboutNakedObjects.getCopyrightNotice()));
        maxWidth = Math.max(maxWidth, fm.stringWidth(AboutNakedObjects.getVersion()));

        width = PADDING + maxWidth + PADDING;
        height = PADDING + logo.getHeight(this) + PADDING + lineHeight * 2 + PADDING;
        Dimension screen = getToolkit().getScreenSize();
        setSize(width, height);

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
        if (logo != null) {
            g.drawImage(logo, PADDING, PADDING, this);
        }

        g.setColor(Color.white);

        g.setFont(font);

        int line = height - PADDING - (2 * lineHeight) + ascent;

        FontMetrics fm = g.getFontMetrics();
        String copyrightNotice = AboutNakedObjects.getCopyrightNotice();
        int left1 = width / 2 - fm.stringWidth(copyrightNotice) / 2;
        String version = AboutNakedObjects.getVersion();
        int left2 = width / 2 - fm.stringWidth(version) / 2;

        int left = Math.min(left1, left2);
        g.drawString(copyrightNotice, left, line);
        line += lineHeight;
        g.drawString(version, left, line);
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
