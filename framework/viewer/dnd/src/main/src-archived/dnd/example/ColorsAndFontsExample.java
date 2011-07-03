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


package org.apache.isis.viewer.dnd.example;

import org.apache.isis.nof.core.context.IsisContext;
import org.apache.isis.nof.core.image.java.AwtTemplateImageLoaderInstaller;
import org.apache.isis.nof.testsystem.TestProxyConfiguration;
import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.image.ImageFactory;
import org.apache.isis.viewer.dnd.viewer.AwtColor;
import org.apache.isis.viewer.dnd.viewer.AwtText;
import org.apache.isis.viewer.dnd.viewer.AwtToolkit;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.WindowAdapter;


public class ColorsAndFontsExample extends Frame {
    private static final int SPACE = 22;
    private Color[] colors;
    private Text[] fonts;

    public ColorsAndFontsExample(final String title) {
        super(title);

        colors = new Color[] { Toolkit.getColor("black"), Toolkit.getColor("white"), Toolkit.getColor("primary1"),
                Toolkit.getColor("primary2"), Toolkit.getColor("primary3"), Toolkit.getColor("secondary1"),
                Toolkit.getColor("secondary2"), Toolkit.getColor("secondary3"),

                Toolkit.getColor("background.application"), Toolkit.getColor("background.window"),
                Toolkit.getColor("background.content-menu"), Toolkit.getColor("background.value-menu"),
                Toolkit.getColor("background.view-menu"), Toolkit.getColor("background.workspace-menu"),

                Toolkit.getColor("menu.normal"), Toolkit.getColor("menu.disabled"), Toolkit.getColor("menu.reversed"),

                Toolkit.getColor("text.edit"), Toolkit.getColor("text.cursor"), Toolkit.getColor("text.highlight"),
                Toolkit.getColor("text.saved"),

                Toolkit.getColor("identified"), Toolkit.getColor("invalid"), Toolkit.getColor("out-of-sync"),
                Toolkit.getColor("error"), Toolkit.getColor("valid"), Toolkit.getColor("active"), };

        fonts = new Text[] { Toolkit.getText(ColorsAndFonts.TEXT_ICON), Toolkit.getText(ColorsAndFonts.TEXT_CONTROL),
                Toolkit.getText(ColorsAndFonts.TEXT_TITLE_SMALL), Toolkit.getText(ColorsAndFonts.TEXT_LABEL),
                Toolkit.getText(ColorsAndFonts.TEXT_MENU), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL),
                Toolkit.getText(ColorsAndFonts.TEXT_STATUS), Toolkit.getText(ColorsAndFonts.TEXT_TITLE) };
    }

    public static void main(final String[] args) {
        IsisContext.setConfiguration(new TestProxyConfiguration());

        new ImageFactory(new AwtTemplateImageLoaderInstaller().createLoader());
        new AwtToolkit();

        final ColorsAndFontsExample f = new ColorsAndFontsExample("Colors and Fonts");
        f.setSize(800, 600);
        f.show();

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(final java.awt.event.WindowEvent e) {
                f.dispose();
            }
        });
    }

    public Insets getInsets() {
        return new Insets(30, 10, 30, 10);
    }

    public void paint(final Graphics g) {
        int x = 10;

        for (int i = 0; i < colors.length; i++) {
            g.setColor(java.awt.Color.black);
            g.drawString(colors[i].getName(), x, 50 + SPACE * i);

            g.setColor(((AwtColor) colors[i]).getAwtColor());
            g.fillRect(x + 200, 40 + SPACE * i, 40, 12);
        }

        x += 300;
        g.setColor(java.awt.Color.black);
        for (int i = 0; i < fonts.length; i++) {
            g.setFont(((AwtText) fonts[i]).getAwtFont());
            g.drawString(fonts[i].getName(), x, 50 + SPACE * i);
            g.drawString("Abcdefghijkl", x + 200, 50 + SPACE * i);

        }

    }
}
