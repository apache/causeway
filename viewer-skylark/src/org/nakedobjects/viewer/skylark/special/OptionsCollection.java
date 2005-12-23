package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.UnexpectedCallException;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;

import java.util.Enumeration;

public class OptionsCollection extends CollectionContent {
    private final NakedObject[] objects;

    public OptionsCollection(NakedObject[] objects) {
        this.objects = objects;
    }

    public Enumeration allElements() {
        return new Enumeration() {
            int index = 0;
            
            public boolean hasMoreElements() {
                return index < objects.length;
            }

            public Object nextElement() {
                return objects[index++];
            }
        };
    }

    public NakedCollection getCollection() {
        throw new UnexpectedCallException();
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(0, "options (array)");
        for (int i = 0; i < objects.length; i++) {
            debug.appendln(2,  objects[i].toString());            
        }
    }

    public Naked drop(Content sourceContent) {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public Image getIconPicture(int iconHeight) {
        return null;
    }

    public Naked getNaked() {
        throw new UnexpectedCallException();
    }

    public NakedObjectSpecification getSpecification() {
        throw new UnexpectedCallException();
    }

    public boolean isTransient() {
        return false;
    }

    public String title() {
        return "";
    }

    public String getDescription() {
        return "";
    }

    public String getId() {
        return "";
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