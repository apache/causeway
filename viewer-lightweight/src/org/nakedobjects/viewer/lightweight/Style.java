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
package org.nakedobjects.viewer.lightweight;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;

import org.apache.log4j.Logger;
import org.nakedobjects.utility.Configuration;


/**
 */
public class Style {
    private static final Logger LOG = Logger.getLogger(Text.class);
    private static final String PROPERTY_STEM = "viewer.lightweight.font.";
    public static final Text TITLE = new Text("title", "SansSerif-bold-12");
    public static final Text NORMAL = new Text("normal", "SansSerif--12");
    public static final Text LABEL = new Text("label", "SansSerif--10");
    public static final Text MENU = new Text("menu", "SansSerif--12");
    public static final Text CLASS = new Text("class", "SansSerif--12");
    public static final Text STATUS = new Text("status", "SansSerif--10");
    public static final Text DEBUG = new Text("debug", "MonoSpaced--10");
    public static final Color APPLICATION_BACKGROUND = new Color("background.application", "#dddddd");
    public static final Color VIEW_BACKGROUND = new Color("background.view", "#FFFFFF");
    public static final Color IN_BACKGROUND = new Color("in-background", "#666666");
    public static final Color IN_FOREGROUND = new Color("in-foreground", "#333333");
    public static final Color IDENTIFIED = new Color("identified", "#0099ff");
    public static final Color ACTIVE = new Color("active", "#6600cc"); //"#0066cc"
    public static final Color VALID = new Color("valid", "#006600");
    public static final Color INVALID = new Color("invalid", "#990000");
	public static final Color FEINT = new Color("feint", "#d0d0d0");
	public static final Color HIGHLIGHT = new Color("hightlight", "#ffcccc");
    public static final Color OTHER = new Color("other", "#000000");
	public static final Color WORKSPACE_MENU = new Color("menu.workspace", "#CCCCCC");
	public static final Color VALUE_MENU = new Color("menu.value", "#CCFFCC");
	public static final Color VIEW_MENU = new Color("menu.view", "#FFCCFF");
	public static final Color OBJECT_MENU = new Color("menu.object", "#FFCCCC");
	public static final Color DISABLED_MENU = new Color("menu.disabled", "#666666");
	public static final Color NORMAL_MENU = new Color("menu.normal", "#000000");
	public static final Color REVERSE_MENU = new Color("menu.reversed", "#FFFFFF");


    public static class Text {
        private Font font;
        private FontMetrics metrics;
        private Frame fontMetricsComponent = new Frame();

        private Text(String propertyName, String defaultFont) {
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
}
