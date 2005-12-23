package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.MenuOptionSet;


public class DummyContent implements Content {

    private String iconName;
    private String title;
    private String windowTitle;

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public void debugDetails(DebugString debug) {}

    public Naked drop(Content sourceContent) {
        return null;
    }

    public Hint getHint() {
        return null;
    }

    public String getIconName() {
        return iconName;
    }

    public Image getIconPicture(int iconHeight) {
        throw new NotImplementedException();
    }

    public Naked getNaked() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }
    
    public boolean isDerived() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isPersistable() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public void menuOptions(MenuOptionSet options) {}

    public void parseTextEntry(String entryText) throws InvalidEntryException {}

    public void setupIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setupTitle(String title) {
        this.title = title;
    }

    public void setupWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String title() {
        return title;
    }

    public String windowTitle() {
        return windowTitle;
    }

    public String getDescription() {
        return null;
    }

    public String getId() {
        return null;
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