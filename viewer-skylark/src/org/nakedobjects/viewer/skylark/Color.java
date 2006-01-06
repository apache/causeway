package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObjects;


public class Color {
    public static final Color DEBUG_BASELINE = new Color(java.awt.Color.magenta);
    public static final Color DEBUG_DRAW_BOUNDS = new Color(java.awt.Color.cyan);
    public static final Color DEBUG_VIEW_BOUNDS = new Color(java.awt.Color.orange);
    public static final Color DEBUG_REPAINT_BOUNDS = new Color(java.awt.Color.green);
    public static final Color DEBUG_BORDER_BOUNDS =  new Color(java.awt.Color.pink);
    
    public static final Color RED = new Color(java.awt.Color.red);
    public static final Color GREEN = new Color(java.awt.Color.green);
    public static final Color BLUE = new Color(java.awt.Color.blue);
    public static final Color BLACK = new Color(java.awt.Color.black);
    public static final Color WHITE = new Color(java.awt.Color.white);
    public static final  Color GRAY = new Color(java.awt.Color.gray);
    public static final Color LIGHT_GRAY = new Color(java.awt.Color.lightGray);
    public static final Color ORANGE = new Color(java.awt.Color.orange);
    public static final Color YELLOW = new Color(java.awt.Color.yellow);
    
    protected static final Color NULL = new Color(0);
    private static final String PROPERTY_STEM = Viewer.PROPERTY_BASE + "color.";
    private java.awt.Color color;
    private String name;

    public Color(int rgbColor) {
        this(new java.awt.Color(rgbColor));
    }

    private Color(java.awt.Color color) {
        this.color = color;
    }

    Color(String propertyName, String defaultColor) {
        this.name = propertyName;
        color = NakedObjects.getConfiguration().getColor(PROPERTY_STEM + propertyName,
                java.awt.Color.decode(defaultColor));
    }

    Color(String propertyName, Color defaultColor) {
        this.name = propertyName;
        color = NakedObjects.getConfiguration().getColor(PROPERTY_STEM + propertyName,
                defaultColor.getAwtColor());
    }

    public Color brighter() {
        return new Color(color.brighter());
    }

    public Color darker() {
        return new Color(color.darker());
    }

    public java.awt.Color getAwtColor() {
        return color;
    }
    
    public String toString() {
        return name + " (" + "#" + Integer.toHexString(color.getRGB()) + ")";
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