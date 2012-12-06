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

package org.apache.isis.runtimes.dflt.runtime.system.internal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.core.runtime.imageloader.TemplateImage;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;

public class SplashWindow extends Window implements Runnable {
    private static final long serialVersionUID = 1L;
    final static Logger LOG = Logger.getLogger(SplashWindow.class);
    private static final String LOGO_TEXT = "Apache Isis";

    private int delay;
    private final Font textFont;
    private final int height;
    private final int textLineHeight;
    private final int titleLineHeight;
    private final Image logo;
    private final int PADDING = 9;
    private final Frame parent;
    private final int width;
    private final Font titleFont;
    private final int left;
    private final Font logoFont;

    public SplashWindow(final TemplateImageLoader loader) {
        super(new Frame());
        parent = (Frame) getParent();
        final String imageName = AboutIsis.getImageName();
        final TemplateImage templateImage = loader.getTemplateImage(imageName);
        if (templateImage == null) {
            throw new IsisException("Failed to find splash image " + imageName);
        }
        logo = templateImage.getImage();

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
            final FontMetrics metrics = getFontMetrics(logoFont);
            width = metrics.stringWidth(LOGO_TEXT);
            height = metrics.getHeight();
        }
        height += PADDING;

        final Dimension text = textBounds();
        width = Math.max(width, text.width);
        height += text.height;

        height = PADDING + height + PADDING;
        width = PADDING + width + PADDING;
        setSize(width, height);

        this.height = height;
        this.width = width;
        this.left = width / 2 - text.width / 2;

        setupCenterLocation();

        setVisible(true);
        // toFront();
    }

    private void setupCenterLocation() {
        final Dimension screen = getToolkit().getScreenSize();
        int x = (screen.width / 2) - (this.width / 2);
        if ((screen.width / screen.height) >= 2) {
            final int f = screen.width / screen.height * 2;
            x = (screen.width / f) - (this.width / 2);
        }
        final int y = (screen.height / 2) - (this.width / 2) - 120;
        setLocation(x, y);
        setBackground(Color.white);
    }

    private Dimension textBounds() {
        final FontMetrics textMetrics = getFontMetrics(textFont);
        final FontMetrics titleMetrics = getFontMetrics(titleFont);
        int width = 0;
        int height = 0;

        // framework details
        width = titleMetrics.stringWidth(AboutIsis.getFrameworkName());
        height += titleLineHeight;
        width = Math.max(width, textMetrics.stringWidth(AboutIsis.getFrameworkCopyrightNotice()));
        height += textLineHeight;
        width = Math.max(width, textMetrics.stringWidth(frameworkVersion()));
        height += textLineHeight;

        // application details
        String text = AboutIsis.getApplicationName();
        if (text != null) {
            width = Math.max(width, titleMetrics.stringWidth(text));
            height += titleLineHeight;
        }
        text = AboutIsis.getApplicationCopyrightNotice();
        if (text != null) {
            width = Math.max(width, textMetrics.stringWidth(text));
            height += textLineHeight;
        }
        text = AboutIsis.getApplicationVersion();
        if (text != null) {
            width = Math.max(width, textMetrics.stringWidth(text));
            height += textLineHeight;
        }

        return new Dimension(width, height);
    }

    @Override
    public void paint(final Graphics g) {
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);

        if (logo != null) {
            g.drawImage(logo, PADDING, PADDING, this);
            // g.drawRect(PADDING, PADDING, logo.getWidth(this) - 1,
            // logo.getHeight(this) - 1);
        } else {
            g.setFont(logoFont);
            final FontMetrics fm = g.getFontMetrics();
            g.drawString(LOGO_TEXT, PADDING, PADDING + fm.getAscent());
        }

        int baseline = height - PADDING - getFontMetrics(textFont).getDescent();

        // framework details - from bottom to top
        g.setFont(textFont);
        g.drawString(frameworkVersion(), left, baseline);
        baseline -= textLineHeight;
        g.drawString(AboutIsis.getFrameworkCopyrightNotice(), left, baseline);
        baseline -= textLineHeight;
        g.setFont(titleFont);
        g.drawString(AboutIsis.getFrameworkName(), left, baseline);
        baseline -= titleLineHeight;

        // application details - from bottom to top
        g.setFont(textFont);
        final String applicationVersion = AboutIsis.getApplicationVersion();
        if (applicationVersion != null) {
            g.drawString(applicationVersion, left, baseline);
            baseline -= textLineHeight;
        }
        final String applicationCopyrightNotice = AboutIsis.getApplicationCopyrightNotice();
        if (applicationCopyrightNotice != null) {
            g.drawString(applicationCopyrightNotice, left, baseline);
            baseline -= textLineHeight;
        }
        final String applicationName = AboutIsis.getApplicationName();
        if (applicationName != null) {
            g.setFont(titleFont);
            g.drawString(applicationName, left, baseline);
        }
    }

    private String frameworkVersion() {
        return AboutIsis.getFrameworkVersion();
    }

    /**
     * leaves the screen up for the specified period (in seconds) and then
     * removes it.
     */
    public void removeAfterDelay(final int seconds) {
        this.delay = seconds * 1000;
        new Thread(this).start();
    }

    public void removeImmediately() {
        hide();
        dispose();
        parent.dispose();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
        }

        removeImmediately();
    }

}
