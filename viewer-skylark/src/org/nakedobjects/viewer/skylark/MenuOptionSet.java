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
package org.nakedobjects.viewer.skylark;

import java.util.Vector;


public class MenuOptionSet {
    public static final int WINDOW = 0;
    public static final int VIEW = 1;
    public static final int OBJECT = 2;
    public static final int EXPLORATION = 3;
    public static final int DEBUG = 4;
    private static final int LAST = DEBUG;
    private Color backgroundColor = Color.DEBUG1;
    private Vector[] set = new Vector[LAST + 1];
    private boolean forView;

    public MenuOptionSet(boolean forView) {
        this.forView = forView;
    }

    /**
     * Specifies the background colour for the menu
     */
    public void setColor(Color color) {
        backgroundColor = color;
    }

    /**
     * Returns the background colour for the menu
     */
    public Color getColor() {
        return backgroundColor;
    }

    /**
     * Determines if the menu is for a view or for whatever the view represents.
     * @return true if for the view itself
     */
    public boolean isForView() {
        return forView;
    }

    public Vector getMenuOptions(boolean includeExploration, boolean includeDebug) {
        Vector v = new Vector();

        createMenu(v, WINDOW);
        createMenu(v, VIEW);
        createMenu(v, OBJECT);

        if (includeExploration) {
        	createMenu(v, EXPLORATION);
        }

        if (includeDebug) {
        	createMenu(v, DEBUG);
        }

        if (v.size() > 0) {
            v.removeElementAt(v.size() - 1);
        }

        return v;
    }

    public void add(int section, UserAction option) {
        if (section < 0 || section > LAST) {
            throw new IllegalArgumentException("Section number out of range!");
        }

        Vector v = set[section];

        if (v == null) {
            v = new Vector();
            set[section] = v;
        }

        v.addElement(option);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MenuOptionSet [\n");
        Vector v = getMenuOptions(true, true);

        for (int i = 0; i < v.size(); i++) {
            sb.append("  element " + i + ". " + v.elementAt(i) + "\n");
        }

        sb.append("]");

        return sb.toString();
    }

    private void createMenu(Vector v, int type) {
        if (set[type] != null) {
            for (int j = 0; j < set[type].size(); j++) {
                v.addElement(set[type].elementAt(j));
            }

            v.addElement(null);
        }
    }
}
