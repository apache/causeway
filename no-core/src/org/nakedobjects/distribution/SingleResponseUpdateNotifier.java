package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.UpdateNotifier;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class SingleResponseUpdateNotifier implements UpdateNotifier {
   private static final Logger LOG = Logger.getLogger(SingleResponseUpdateNotifier.class);
    private ObjectDataFactory factory;
    private Vector updates = new Vector();

    public void broadcastObjectChanged(NakedObject object) {
        LOG.debug("Update captured for " + object);
        
        Vector copy;
        synchronized(updates) {
            copy = (Vector) updates.clone();
        }
        
        Enumeration e = copy.elements();
        while (e.hasMoreElements()) {
            UpdatePackager packager = (UpdatePackager) e.nextElement();
            ObjectData objectData = factory.createObjectData(object, 1);
            packager.addUpdate(objectData);
        }
    }
    
    public UpdatePackager createUpdatePackager() {
        UpdatePackager updatePackager = new UpdatePackager();
        updates.addElement(updatePackager);
        return updatePackager;
    }

    public void remove(UpdatePackager updatePackager) {
        updates.removeElement(updatePackager);
    }
    
    public void setFactory(ObjectDataFactory factory) {
        this.factory = factory;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Factory(ObjectDataFactory factory) {
        this.factory = factory;
    }

    public void shutdown() {}
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