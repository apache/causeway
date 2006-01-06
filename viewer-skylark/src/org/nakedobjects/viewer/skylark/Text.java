package org.nakedobjects.viewer.skylark;

import java.awt.Font;


public interface Text {

    /**
     * Returns the widths, in pixels, of the specified character.
     */
    int charWidth(char c);

    /**
     * Returns the height, in pixels, of the distance from the baseline to top
     * of the tallest character.
     */
    int getAscent();

    /**
     * Returns the Font from the AWT used for drawing within the AWT.
     * 
     * @see Font
     */
    Font getAwtFont();

    /**
     * Returns the height, in pixels, of the distance from bottom of the lowest
     * descending character to the baseline.
     */
    int getDescent();

    /**
     * Returns the mid point, in pixels, between the baseline and the top of the characters.
     */
    int getMidPoint();
    
    /**
     * Returns the height, in pixels, for a normal line of text - where there is
     * some space between two lines of text.
     */
    int getTextHeight();

    /**
     * Returns the sum of the text height and line spacing.
     * 
     * @see #getLineHeight()
     * @see #getLineSpacing()
     */
    int getLineHeight();

    /**
     * Returns the number of blank vertical pixels to add between adjacent lines
     * to give them additional spacing.
     */
    int getLineSpacing();

    /**
     * Returns the width of the specified in pixels.
     */
    int stringWidth(String text);
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