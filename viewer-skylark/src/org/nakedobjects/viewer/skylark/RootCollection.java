package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.util.ImageFactory;

import java.util.Enumeration;


public class RootCollection extends CollectionContent {
    private final NakedCollection collection;

    public RootCollection(NakedCollection collection) {
        this.collection = collection;
    }

    public Enumeration allElements() {
        return getCollection().elements();
    }

    public Consent canClear() {
        return Veto.DEFAULT;
    }

    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(4, "collection", collection);
    }

    public NakedCollection getCollection() {
        return collection;
    }

    public boolean isCollection() {
        return true;
    }

    public String getDescription() {
        return "";
    }

    public String getIconName() {
        return null;
    }

    public String getId() {
        return "";
    }

    public Naked getNaked() {
        return collection;
    }

    public NakedObjectSpecification getSpecification() {
        return collection.getSpecification();
    }

    public boolean isTransient() {
        return collection != null;
    }

    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public String title() {
        return collection.titleString();
    }

    public String windowTitle() {
        return collection.titleString();
    }

    public String toString() {
        return "Root Collection: " + collection;
    }

    public Naked drop(Content sourceContent) {
        return null;
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public Image getIconPicture(int iconHeight) {
        // return ImageFactory.getInstance().loadObjectIcon(getSpecification(), "", iconHeight);
        return ImageFactory.getInstance().createIcon("root-collection", iconHeight, null);
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