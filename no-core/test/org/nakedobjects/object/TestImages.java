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

import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import org.apache.log4j.PropertyConfigurator;


public class TestImages extends Frame {
	
    String[] files;
    private Image[] images;
    private int HEIGHT = 60;
    private int WIDTH = 120;

    public TestImages() {
        String dir = ImageIcon.getDirectory();
        files = new File(dir).list();
        images = new Image[files.length];

        for (int i = 0; i < files.length; i++) {
            String file = files[i]; //.substring(0,files[i].length() - 6);
            images[i] = ImageIcon.loadImage(file);

            //           System.out.println(file + " " + image);
        }

        setSize(300, 300);
        show();
    }

    public static void main(String[] args) {
    	PropertyConfigurator.configure("log4j.testing.properties");
        new TestImages();
    }

    /*
     * @see java.awt.Container#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int x = 0;
        int y = 0;

        g.setFont(new Font("Sans", 0, 9));

        for (int i = 0; i < images.length; i++) {
            Image image = images[i];

            if (image != null) {
                g.drawImage(image, x, y, 32, 32,null);
            }

            g.drawString(files[i], x, y + HEIGHT - 3);

            x = x + WIDTH;

            if (x >= width) {
                x = 0;
                y = y + HEIGHT;

                if (y >= height) {
                    break;
                }
            }
        }
    }
}
