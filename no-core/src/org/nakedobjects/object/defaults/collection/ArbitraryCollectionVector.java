package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.ArbitraryNakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.defaults.value.TextString;


public class ArbitraryCollectionVector extends AbstractNakedCollectionVector implements ArbitraryNakedCollection {
    private final TextString name = new TextString();

    public ArbitraryCollectionVector() {
        super();
    }

    /**
     * Constructs collection and sets the name.
     */
    public ArbitraryCollectionVector(final String name) {
        this();
        setName(name);
    }

    public Permission canAdd(NakedObject object) {
        if (object == this) {
            return new Veto("Cannot add self");
        } else {
            return Permission.create(!contains(object), "", "Cannot add a duplicate object");
        }
    }

    public Permission canRemove(NakedObject object) {
        return Permission.create(contains(object), "", "Object is not in collection");
    }

    public TextString getName() {
        return name;
    }

    public void setName(final String name) {
        this.name.setValue(name);
    }

    public String titleString() {
        return name.stringValue();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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