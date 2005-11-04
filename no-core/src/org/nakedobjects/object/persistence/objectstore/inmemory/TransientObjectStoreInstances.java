package org.nakedobjects.object.persistence.objectstore.inmemory;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Oid;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


/*
 * The objects need to store in a repeatable sequence so the
 * instances method return the same data for any repeated call, and so that one
 * subset of instances follows on the previous. This is done by keeping the
 * objects in the order that they where created.
 */
class TransientObjectStoreInstances {
    protected final Vector objectInstances = new Vector();
    protected final Hashtable titleIndex = new Hashtable();

    protected void finalize() throws Throwable {
        super.finalize();
        Logger.getLogger(TransientObjectStoreInstances.class).info("finalizing instances");
    }

    public boolean hasInstances() {
        return numberOfInstances() > 0;
    }

    public void instances(Vector instances) {
        Enumeration e = objectInstances.elements();
        while (e.hasMoreElements()) {
            Oid oid = (Oid) e.nextElement();
	        instances.addElement(oid);
        }
    }

    public int numberOfInstances() {
        return objectInstances.size();
    }

    public void remove(Oid oid) {
        objectInstances.removeElement(oid);
    }

    public void add(NakedObject object) {
        objectInstances.addElement(object.getOid());
    }
    
    public void save(NakedObject object) {
        titleIndex.put(object.titleString().toLowerCase(), object.getOid());
    }

    public void shutdown() {
        objectInstances.removeAllElements();
        titleIndex.clear();
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