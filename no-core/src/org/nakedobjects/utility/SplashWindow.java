package org.nakedobjects.utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import org.nakedobjects.object.ImageIcon;


public class SplashWindow extends Window implements Runnable {
    private final int WIDTH = 340;
    private final int HEIGHT = 400;
    private final int LEFT = 55;
    private final String FONT = "SansSerif--9";
    private Frame parent;
    private Image logo;
	private int delay;

    public SplashWindow() {
        super(new Frame());
        parent = (Frame) getParent();
        logo = ImageIcon.loadImage("logo.jpg");
        
        Dimension screen = getToolkit().getScreenSize();
        setSize(WIDTH, HEIGHT);

        int x = (screen.width / 2) - (WIDTH / 2);

        if ((screen.width / screen.height) >= 2) {
            x = (screen.width / 4) - (WIDTH / 2);
        }

        int y = (screen.height / 2) - (HEIGHT / 2);
        setLocation(x, y);
        setBackground(Color.black);

        show();
        toFront();
    }

    public void removeImmediately() {
    	hide();
    	dispose();
    	parent.dispose();
    }
    
    /**
     * leaves the screen up for the specified period (in seconds) and then
     * removes it.
     */
    public void removeAfterDelay(int seconds) {
    	this.delay = seconds * 1000;
    	new Thread(this).start();
    }
    
    public void paint(Graphics g) {
       if(logo != null) {  g.drawImage(logo, 20, 10, this);}

        g.setColor(Color.white);
        
        g.setFont(Font.decode(FONT));

        FontMetrics fm = g.getFontMetrics();
        int lineheight = (int) (fm.getHeight() * 1.05);
        int line = HEIGHT - (4 * lineheight);

        String version = "@VERSION@";
        g.drawString("Naked Objects, version " + version, LEFT, line);
        line += lineheight;
        g.drawString("Copyright \u00a9 2000 - 2004 Naked Objects Group Ltd", LEFT, line);
        line += lineheight;
        g.drawString("This framework is released under the GPL", LEFT, line);
        line += lineheight;
        g.drawString("http://www.nakedobjects.org", LEFT, line);
    }

    public void run() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }

        removeImmediately();
    }
}

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
