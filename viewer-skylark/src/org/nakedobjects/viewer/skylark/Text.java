package org.nakedobjects.viewer.skylark;

import org.nakedobjects.container.configuration.Configuration;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;

import org.apache.log4j.Logger;

public class Text {
    private static final Logger LOG = Logger.getLogger(Text.class);
    private static final String PROPERTY_STEM = Viewer.PROPERTY_BASE + "font.";
    private Font font;
    private FontMetrics metrics;
    private Frame fontMetricsComponent = new Frame();

    protected Text(String propertyName, String defaultFont) {
        Configuration cfg = Configuration.getInstance();
        font = cfg.getFont(PROPERTY_STEM + propertyName, Font.decode(defaultFont));
        LOG.info("font " + propertyName + " loaded as " + font);

        if (font == null) {
            font = cfg.getFont(PROPERTY_STEM + "default", new Font("SansSerif", Font.PLAIN, 12));
        }

        metrics = fontMetricsComponent.getFontMetrics(font);

        LOG.debug("font " + propertyName + " height=" + metrics.getHeight() + ", leading=" +
            metrics.getLeading() + ", ascent=" + metrics.getAscent() + ", descent=" +
            metrics.getDescent());
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

    public int getHeight() {
        return metrics.getHeight();
    }

    public int charWidth(char c) {
        return metrics.charWidth(c);
    }

    public int stringWidth(String text) {
        return metrics.stringWidth(text);
    }

    public String toString() {
        return font.toString();
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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