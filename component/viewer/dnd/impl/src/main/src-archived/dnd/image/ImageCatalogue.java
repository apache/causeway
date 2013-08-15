/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.viewer.dnd.image;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImageCatalogue extends Frame {
    private static final String DIRECTORY = "src/images/";
    private String[] files;
    private Image[] images;
    private int HEIGHT = 60;
    private int WIDTH = 120;
    private static MediaTracker mt = new MediaTracker(new Canvas());

    public ImageCatalogue() {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
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

            if (images[i] == null) {
                System.err.println(file + " not loaded");
            }
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                dispose();
            }
        });
        setSize(300, 300);
        show();
    }

    public static void main(final String[] args) {
        // PropertyConfigurator.configure("log4j.testing.properties");
        BasicConfigurator.configure();
        new ImageCatalogue();
    }

    public void paint(final Graphics g) {
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
