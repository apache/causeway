package org.nakedobjects.viewer.skylark;

/**
 */
public class Style {

    // colors
    public static final Color APPLICATION_BACKGROUND = new Color("background.application", "#dddddd");
    public static final Color BLACK = new Color("black", "#000000");
    public static final Text CLASS = new Text("class", "SansSerif--12");
    public static final Color CONTENT_MENU = new Color("menu.content", "#FFCCCC");
    public static final Text DEBUG = new Text("debug", "MonoSpaced--10");
    public static final Color DISABLED_MENU = new Color("menu.disabled", "#666666");

    public static final Color IDENTIFIED = new Color("identified", "#0099ff");
    public static final Color INVALID = new Color("invalid", "#ffc4c4");
    public static final Text LABEL = new Text("label", "SansSerif--10");
    public static final Text MENU = new Text("menu", "SansSerif--12");
    public static final Text NORMAL = new Text("normal", "SansSerif--12");
    public static final Color NORMAL_MENU = new Color("menu.normal", "#000000");
    public static final Color PRIMARY1 = new Color("primary1", "#666699"); //"#0066cc"
    public static final Color PRIMARY2 = new Color("primary2", "#9999cc");
    public static final Color PRIMARY3 = new Color("primary3", "#ccccff");
    public static final Color TEXT_EDIT = new Color("textedit", "#669966");
    public static final Color REVERSE_MENU = new Color("menu.reversed", "#FFFFFF");
    public static final Color SECONDARY1 = new Color("secondary1", "#666666");
    public static final Color SECONDARY2 = new Color("secondary2", "#999999");
    public static final Color SECONDARY3 = new Color("secondary3", "#cccccc");
    public static final Text STATUS = new Text("status", "SansSerif--10");

    // fonts
    public static final Text TITLE = new Text("title", "SansSerif-bold-12");
    public static final Color VALID = new Color("valid", "#006600");
    public static final Color VALUE_MENU = new Color("menu.value", "#CCFFCC");
    public static final Color VIEW_MENU = new Color("menu.view", "#FFCCFF");
    public static final Color WHITE = new Color("white", "#ffffff");

    public static final Color WORKSPACE_MENU = new Color("menu.workspace", "#CCCCCC");
    public static final Color OUT_OF_SYNCH = new Color("out-of-sync", "#662200");

    public static int defaultBaseline() {
        int iconSize = Style.NORMAL.getAscent() * 120 / 100;
        int height = View.VPADDING + iconSize;

        return height;
    }

    public static int defaultFieldHeight() {
        int iconSize = Style.NORMAL.getHeight() * 120 / 100;
        int height = View.VPADDING * 2 + iconSize;

        return height;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
