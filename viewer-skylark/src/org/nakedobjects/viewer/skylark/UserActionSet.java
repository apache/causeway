package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;

import java.util.Vector;


public class UserActionSet implements UserAction {
    private Color backgroundColor = Color.DEBUG_BASELINE;
    private final String groupName;
    private final boolean includeDebug;
    private final boolean includeExploration;
    private Vector options = new Vector();
    private final Type type;

    public UserActionSet(boolean includeExploration, boolean includeDebug, Type type) {
        this.type = type;
        this.groupName = "";
        this.includeExploration = includeExploration;
        this.includeDebug = includeDebug;
    }

    public UserActionSet(String groupName, UserActionSet parent) {
        this.groupName = groupName;
        this.includeExploration = parent.includeExploration;
        this.includeDebug = parent.includeDebug;
        this.type = parent.type;
        this.backgroundColor = parent.getColor();
    }

    /**
     * Add the specified option if it is of the right type for this menu.
     */
    public void add(UserAction option) {
        Type section = option.getType();
        if (section == USER || (includeExploration && section == EXPLORATION) || (includeDebug && section == DEBUG)) {
            options.addElement(option);
        }
    }

    public Consent disabled(View view) {
        return Allow.DEFAULT;
    }

    public void execute(Workspace workspace, View view, Location at) {}

    /**
     * Returns the background colour for the menu
     */
    public Color getColor() {
        return backgroundColor;
    }

    public String getDescription(View view) {
        return "";
    }

    public UserAction[] getMenuOptions() {
        UserAction[] v = new UserAction[options.size()];
        for (int i = 0; i < v.length; i++) {
            v[i] = (UserAction) options.elementAt(i);
        }
        return v;
    }

    public String getName(View view) {
        return groupName;
    }

    public Type getType() {
        return type;
    }

    /**
     * Specifies the background colour for the menu
     */
    public void setColor(Color color) {
        backgroundColor = color;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MenuOptionSet [");
        for (int i = 0, size = options.size(); i < size; i++) {
            sb.append(options.elementAt(i) + ",");
        }
        sb.append("]");
        return sb.toString();
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