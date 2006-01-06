package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.utility.NakedObjectConfiguration;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;

import org.apache.log4j.Logger;


public class AwtText implements Text {
    private static final String ASCENT_ADJUST = Viewer.PROPERTY_BASE + "ascent-adjust";
    private static final String FONT_PROPERTY_STEM = Viewer.PROPERTY_BASE + "font.";
    private static final Logger LOG = Logger.getLogger(AwtText.class);
    private static final String SPACING_PROPERTYSTEM = Viewer.PROPERTY_BASE + "spacing.";
    private boolean ascentAdjust;
    private Font font;
    private Frame fontMetricsComponent = new Frame();
    private int lineSpacing;
    private int maxCharWidth;
    private FontMetrics metrics;

    protected AwtText(String propertyName, String defaultFont) {
        NakedObjectConfiguration cfg = NakedObjects.getConfiguration();
        font = cfg.getFont(FONT_PROPERTY_STEM + propertyName, Font.decode(defaultFont));
        LOG.info("font " + propertyName + " loaded as " + font);

        if (font == null) {
            font = cfg.getFont(FONT_PROPERTY_STEM + "default", new Font("SansSerif", Font.PLAIN, 12));
        }

        metrics = fontMetricsComponent.getFontMetrics(font);

        maxCharWidth = metrics.getMaxAdvance() + 1;
        if (maxCharWidth == 0) {
            maxCharWidth = (charWidth('X') + 3);
        }

        lineSpacing = cfg.getInteger(SPACING_PROPERTYSTEM + propertyName, 0);

        ascentAdjust = cfg.getBoolean(ASCENT_ADJUST, false);

        LOG.debug("font " + propertyName + " height=" + metrics.getHeight() + ", leading=" + metrics.getLeading() + ", ascent="
                + metrics.getAscent() + ", descent=" + metrics.getDescent() + ", line spacing=" + lineSpacing);
    }

    public int charWidth(char c) {
        return metrics.charWidth(c);
    }

    public int getAscent() {
        return metrics.getAscent() - (ascentAdjust ? metrics.getDescent() : 0);
    }

    public Font getAwtFont() {
        return font;
    }

    public int getDescent() {
        return metrics.getDescent();
    }

    public int getLineHeight() {
        return metrics.getHeight() + getLineSpacing();
    }

    public int getLineSpacing() {
        return lineSpacing;
    }

    public int getMidPoint() {
        return getAscent() / 2;
    }

    public int getTextHeight() {
        return metrics.getHeight()  - (ascentAdjust ? metrics.getDescent() : 0);
    }

    public int stringWidth(String text) {
        int stringWidth = metrics.stringWidth(text);
        if (stringWidth > text.length() * maxCharWidth) {
            LOG.debug("spurious width of string; calculating manually: " + stringWidth + " for " + this + ": " + text);
            /*
             * This fixes an intermittent bug in .NET where stringWidth() returns a ridiculous number is
             * returned for the width.
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

    public String toString() {
        return font.toString();
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