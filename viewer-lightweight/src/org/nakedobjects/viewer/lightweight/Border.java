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

public interface Border {
    //    int getBaseline(ObjectView view);

    /**
     * Determines the size of border in terms of it left and right widths, and top and bottom heights.
     * @param view  the view this border is being placed around
     */
    Padding getPadding(View view);

    /**
     * Determines the mininum widths of top and bottom borders, and heights of the left and right borders.
     * @param view  the view this border is being placed around
     */
    String debug(View view);

    /**
     * Determines if the user is invoking an action relating to this view, rather than to whatever this view represents.
     * @return true if the user is targeting the view itself, false if the user is targeting what is being represented
     */
    void firstClick(View view, Click click);

    void draw(View view, Canvas canvas);

    void secondClick(View view, Click click);

    /**
     * Called when the popup menu is being populated for this object's border.  Any object
     * options that need to appear on the menu should be added to the
     * <code>menuOptions</code> object.
     * @param menuOptions
     */

    //	void objectMenuOptions(ObjectView view, MenuOptionSet menuOptions);

    /**
     * Called when the popup menu is being populated for this view's border.  Any view
     * options that need to appear on the menu should be added to the
     * <code>menuOptions</code> object.
     * @param menuOptions
     */
    void viewMenuOptions(View view, MenuOptionSet menuOptions);
}
