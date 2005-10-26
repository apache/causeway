package org.nakedobjects.distribution;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.utility.ToString;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class SingleResponseUpdateNotifier implements DirtyObjectSet {
    private static final Logger LOG = Logger.getLogger(SingleResponseUpdateNotifier.class);
    private Vector updates = new Vector();

    public void addDirty(NakedObject object) {
        if (!updates.contains(object)) {
            LOG.debug("update of " + object);
            updates.addElement(object);
        }
    }

    public NakedObject[] getUpdates() {
        int noUpdates = updates.size();
        LOG.debug(noUpdates + " updates for request");
        NakedObject[] updatesArray = new NakedObject[noUpdates];

        int i = 0;
        for (Enumeration e = updates.elements(); e.hasMoreElements();) {
            NakedObject object = (NakedObject) e.nextElement();
            updatesArray[i++] = object;
        }
        updates.removeAllElements();
        return updatesArray;
    }

    public void clearUpdates() {
        updates.removeAllElements();
    }
    
    public void init() {}

    public void shutdown() {}

    public String toString() {
        ToString str = new ToString(this);
        str.append("updates", updates);
        return str.toString();
    }

    public String updateList() {
        return updates.toString();
    }

    public void removeUpdateFor(NakedObject object) {
        updates.removeElement(object);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */