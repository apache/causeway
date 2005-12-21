package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedObject;

import java.util.Vector;

public class ClientSideTransaction {
    private final Vector persisted = new Vector();
    private final Vector changed = new Vector();
    private final Vector deleted  = new Vector();
    
    public void addObjectChanged(NakedObject object) {
        if(! changed.contains(object)) {
            changed.addElement(object);
        }
    }

    public void rollback() {
        // TODO need to restore the state of all involved objects
    }

    public void addDestroyObject(NakedObject object) {
        if(! deleted.contains(object)) {
            deleted.addElement(object);
        }
    }

    public void addMakePersistent(NakedObject object) {
        if(! persisted.contains(object)) {
            persisted.addElement(object);
        }
    }

    public NakedObject[] getDeleted() {
        NakedObject[] array = new NakedObject[deleted.size()];
        deleted.copyInto(array);
        return array;
    }

    public NakedObject[] getChanged() {
        NakedObject[] array = new NakedObject[changed.size()];
        changed.copyInto(array);
        return array;
    }

    public NakedObject[] getPersisted() {
        NakedObject[] array = new NakedObject[persisted.size()];
        persisted.copyInto(array);
        return array;
    }

    public boolean isEmpty() {
        return persisted.size() == 0 && changed.size() == 0 && deleted.size() == 0;
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