package org.nakedobjects.object.loader;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.utility.Assert;


/**
 * Provides a simple key for reliably getting hold of the adapter for an 'internal' collection used by a
 * domain object. Combines the parent object (the domain object) and the collection field to create an
 * unchanging key.
 */
class InternalCollectionKey {
    private final NakedObject parent;
    private final String fieldName;

    public InternalCollectionKey(NakedObject parent, String fieldName) {
        Assert.assertNotNull("parent", parent);
        Assert.assertNotNull("fieldName", fieldName);
        
        this.parent = parent;
        this.fieldName = NameConvertor.simpleName(fieldName);
    }

    public int hashCode() {
        return parent.hashCode() + fieldName.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof InternalCollectionKey) {
            InternalCollectionKey other = (InternalCollectionKey) obj;
            return other.parent.equals(parent) && other.fieldName.equals(fieldName);
        } else {
            return false;
        }
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