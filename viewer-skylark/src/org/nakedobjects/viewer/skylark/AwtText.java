package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;

import org.apache.log4j.Logger;

public class AwtText implements Text {
    private static final Logger LOG = Logger.getLogger(AwtText.class);
    private static final String FONT_PROPERTY_STEM = Viewer.PROPERTY_BASE + "font.";
    private static final String SPACING_PROPERTY_STEM = Viewer.PROPERTY_BASE + "spacing.";
    private final int maxCharWidth;
    private Font font;
    private FontMetrics metrics;
    private Frame fontMetricsComponent = new Frame();
    private int lineSpacing;

    protected AwtText(String propertyName, String defaultFont) {
        Configuration cfg = NakedObjects.getConfiguration();
        font = cfg.getFont(FONT_PROPERTY_STEM + propertyName, Font.decode(defaultFont));
        LOG.info("font " + propertyName + " loaded as " + font);

        if (font == null) {
            font = cfg.getFont(FONT_PROPERTY_STEM + "default", new Font("SansSerif", Font.PLAIN, 12));
        }

        metrics = fontMetricsComponent.getFontMetrics(font);
        
        maxCharWidth = (charWidth('X') + 3);

        lineSpacing = cfg.getInteger(SPACING_PROPERTY_STEM + propertyName, 0);
        
        LOG.debug("font " + propertyName + " height=" + metrics.getHeight() + ", leading=" +
            metrics.getLeading() + ", ascent=" + metrics.getAscent() + ", descent=" +
            metrics.getDescent()  + ", line spacing=" + lineSpacing);
    }

    public int getAscent() {
        return metrics.getAscent();
    }

    public Font getAwtFont() {
        return font;
    }

    public int getDescent() {
        return metrics.getDescent();
    }

    public int getTextHeight() {
        return metrics.getHeight();
    }

    public int charWidth(char c) {
        return metrics.charWidth(c);
    }

    public int stringWidth(String text) {
        int stringWidth = metrics.stringWidth(text);
        if(stringWidth > text.length() * maxCharWidth) {
            LOG.debug("spurious length of string; calculating manually: " + stringWidth  + " for "+ this);
            /*
             * This fixes an intermittent bug in .NET where stringWidth() returns a ridiculous number is returned for the width.
             * 
             * TODO don't do this when running Java
             */
            stringWidth = 0;
            for (int i = 0; i < text.length(); i++) {
                stringWidth += charWidth(text.charAt(i));
                LOG.debug(i + " " + stringWidth);
            }
        }
        return stringWidth;
    }

    public String toString() {
        return font.toString();
    }

    public int getLineHeight() {
        return getTextHeight() + getLineSpacing();
    }

    public int getLineSpacing() {
        return lineSpacing;
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