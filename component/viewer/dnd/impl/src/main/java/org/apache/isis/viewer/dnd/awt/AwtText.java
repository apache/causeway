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

package org.apache.isis.viewer.dnd.awt;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.util.Properties;

public class AwtText implements Text {
    private static final String ASCENT_ADJUST = Properties.PROPERTY_BASE + "ascent-adjust";
    private static final String FONT_PROPERTY_STEM = Properties.PROPERTY_BASE + "font.";
    private static final Logger LOG = LoggerFactory.getLogger(AwtText.class);
    private static final String SPACING_PROPERTYSTEM = Properties.PROPERTY_BASE + "spacing.";
    private final boolean ascentAdjust;
    private Font font;
    private final Frame fontMetricsComponent = new Frame();
    private final int lineSpacing;
    private int maxCharWidth;
    private final FontMetrics metrics;
    private final String propertyName;

    protected AwtText(final String propertyName, final String defaultFont) {
        final IsisConfiguration cfg = IsisContext.getConfiguration();
        font = cfg.getFont(FONT_PROPERTY_STEM + propertyName, Font.decode(defaultFont));
        LOG.info("font " + propertyName + " loaded as " + font);

        this.propertyName = propertyName;

        if (font == null) {
            font = cfg.getFont(FONT_PROPERTY_STEM + ColorsAndFonts.TEXT_DEFAULT, new Font("SansSerif", Font.PLAIN, 12));
        }

        metrics = fontMetricsComponent.getFontMetrics(font);

        maxCharWidth = metrics.getMaxAdvance() + 1;
        if (maxCharWidth == 0) {
            maxCharWidth = (charWidth('X') + 3);
        }

        lineSpacing = cfg.getInteger(SPACING_PROPERTYSTEM + propertyName, 0);

        ascentAdjust = cfg.getBoolean(ASCENT_ADJUST, false);

        LOG.debug("font " + propertyName + " height=" + metrics.getHeight() + ", leading=" + metrics.getLeading() + ", ascent=" + metrics.getAscent() + ", descent=" + metrics.getDescent() + ", line spacing=" + lineSpacing);
    }

    @Override
    public int charWidth(final char c) {
        return metrics.charWidth(c);
    }

    @Override
    public int getAscent() {
        return metrics.getAscent() - (ascentAdjust ? metrics.getDescent() : 0);
    }

    /**
     * Returns the Font from the AWT used for drawing within the AWT.
     * 
     * @see Font
     */
    public Font getAwtFont() {
        return font;
    }

    @Override
    public int getDescent() {
        return metrics.getDescent();
    }

    @Override
    public int getLineHeight() {
        return metrics.getHeight() + getLineSpacing();
    }

    @Override
    public int getLineSpacing() {
        return lineSpacing;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public int getMidPoint() {
        return getAscent() / 2;
    }

    @Override
    public int getTextHeight() {
        return metrics.getHeight() - (ascentAdjust ? metrics.getDescent() : 0);
    }

    @Override
    public int stringHeight(final String text, final int maxWidth) {
        int noLines = 0;
        final StringTokenizer lines = new StringTokenizer(text, "\n\r");
        while (lines.hasMoreTokens()) {
            final String line = lines.nextToken();
            final StringTokenizer words = new StringTokenizer(line, " ");
            final StringBuffer l = new StringBuffer();
            int width = 0;
            while (words.hasMoreTokens()) {
                final String nextWord = words.nextToken();
                final int wordWidth = stringWidth(nextWord);
                width += wordWidth;
                if (width >= maxWidth) {
                    noLines++;
                    l.setLength(0);
                    width = wordWidth;
                }
                l.append(nextWord);
                l.append(" ");
                width += stringWidth(" ");
            }
            noLines++;
        }
        return noLines * getLineHeight();
    }

    @Override
    public int stringWidth(final String text, final int maxWidth) {
        int width = 0;
        final StringTokenizer lines = new StringTokenizer(text, "\n\r");
        while (lines.hasMoreTokens()) {
            final String line = lines.nextToken();
            final StringTokenizer words = new StringTokenizer(line, " ");
            final StringBuffer l = new StringBuffer();
            int lineWidth = 0;
            while (words.hasMoreTokens()) {
                final String nextWord = words.nextToken();
                final int wordWidth = stringWidth(nextWord);
                lineWidth += wordWidth;
                if (lineWidth >= maxWidth) {
                    return maxWidth;
                }
                if (lineWidth > width) {
                    width = lineWidth;
                }
                l.append(nextWord);
                l.append(" ");
                lineWidth += stringWidth(" ");
            }
        }
        return width;
    }

    // DKH: 20060404... yes, this will grow over time, but only used client-side
    // RCM this is only a temporary solutions to help deal with titles, TODO
    // move the caching up to the
    // views/components that use this method
    private final java.util.Hashtable stringWidthByString = new java.util.Hashtable();

    // DKH 20060404: new implementation that caches
    @Override
    public int stringWidth(final String text) {
        int[] cachedStringWidth = (int[]) stringWidthByString.get(text);
        if (cachedStringWidth == null) {
            cachedStringWidth = new int[] { stringWidthInternal(text) };
            stringWidthByString.put(text, cachedStringWidth);
        }
        return cachedStringWidth[0];
    }

    // DKH 20060404: previously was stringWidth, now cached, see above.
    private int stringWidthInternal(final String text) {
        int stringWidth = metrics.stringWidth(text);
        if (stringWidth > text.length() * maxCharWidth) {
            LOG.debug("spurious width of string; calculating manually: " + stringWidth + " for " + this + ": " + text);
            /*
             * This fixes an intermittent bug in .NET where stringWidth()
             * returns a ridiculous number is returned for the width.
             * 
             * TODO don't do this when running Java
             */
            stringWidth = 0;
            for (int i = 0; i < text.length(); i++) {
                int charWidth = charWidth(text.charAt(i));
                if (charWidth > maxCharWidth) {
                    LOG.debug("spurious width of character; using max width: " + charWidth + " for " + text.charAt(i));
                    charWidth = maxCharWidth;
                }
                stringWidth += charWidth;
                LOG.debug(i + " " + stringWidth);
            }
        }
        return stringWidth;
    }

    @Override
    public String toString() {
        return font.toString();
    }

    public static String defaultFontFamily() {
        final IsisConfiguration cfg = IsisContext.getConfiguration();
        return cfg.getString(FONT_PROPERTY_STEM + "family", "SansSerif");
    }

    public static int defaultFontSizeSmall() {
        final IsisConfiguration cfg = IsisContext.getConfiguration();
        return cfg.getInteger(FONT_PROPERTY_STEM + "size.small", 10);
    }

    public static int defaultFontSizeMedium() {
        final IsisConfiguration cfg = IsisContext.getConfiguration();
        return cfg.getInteger(FONT_PROPERTY_STEM + "size.medium", 11);
    }

    public static int defaultFontSizeLarge() {
        final IsisConfiguration cfg = IsisContext.getConfiguration();
        return cfg.getInteger(FONT_PROPERTY_STEM + "size.large", 12);
    }
}
