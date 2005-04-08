import java.awt.Canvas;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import org.apache.log4j.BasicConfigurator;


public class ImageCatalogue extends Frame {
	private static final String DIRECTORY = "example/images/";
	private String[] files;
    private Image[] images;
    private int HEIGHT = 60;
   private int WIDTH = 120;
	private static MediaTracker mt = new MediaTracker(new Canvas());

    public ImageCatalogue() {
    	File dir = new File(DIRECTORY);
    	if(!dir.exists()) {
    		throw new RuntimeException("No directory " + dir);
    	}
		System.out.println(dir.getAbsolutePath());
        files = dir.list();
        images = new Image[files.length];

        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            String url = DIRECTORY + file;
            Image image = Toolkit.getDefaultToolkit().createImage(url);
			if (image != null) {
				mt.addImage(image, 0);
				try {
					mt.waitForAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mt.isErrorAny()) {
					System.err.println("Failed to load image from resource: " + url + " " + mt.getErrorsAny()[0]);
                
					mt.removeImage(image);
					image = null;
				}
			}
			System.out.println("Image " + image + " loaded from " + url);


			images[i] = image;
		
			if(images[i] == null) {
            	System.err.println(file + " not loaded");
            }
        }

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        setSize(300, 300);
        show();
    }

    public static void main(String[] args) {
 //       PropertyConfigurator.configure("log4j.testing.properties");
        BasicConfigurator.configure();
        new ImageCatalogue();
    }

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int x = getInsets().left;
        int y = getInsets().top;

        g.setFont(new Font("Sans", 0, 9));

        for (int i = 0; i < images.length; i++) {
            Image image = images[i];

            if (image != null) {
                g.drawImage(image, x, y, 32, 32, null);
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


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
