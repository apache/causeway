package org.nakedobjects.object;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class DirtyObjectSetImpl implements DirtyObjectSet {
    private static final Logger LOG = Logger.getLogger(DirtyObjectSetImpl.class);
    private Vector changes = new Vector();

    public synchronized void addDirty(NakedObject object) {
        LOG.debug("mark as dirty " + object);
        if (!changes.contains(object)) {
            changes.addElement(object);
        }
    }

    public synchronized Enumeration dirtyObjects() {
        Enumeration changedObjects = changes.elements();
        if (changes.size() > 0) {
            LOG.debug("dirty objects " + changes);
        }
        changes = new Vector();
        return changedObjects;
    }

    public void shutdown() {
        LOG.info("  shutting down " + this);
        changes.removeAllElements();
        changes = null;
    }

    public void remove(NakedObject object) {
        LOG.debug("unmark as dirty " + object);
        changes.removeElement(object);
    }

    public void remove(NakedObject[] instances) {
        for (int i = 0; i < instances.length; i++) {
            remove(instances[i]);
        }
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