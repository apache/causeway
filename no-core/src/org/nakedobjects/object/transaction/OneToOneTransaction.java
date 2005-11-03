package org.nakedobjects.object.transaction;

import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectPersistenceManager;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.AbstractOneToOnePeer;
import org.nakedobjects.object.reflect.OneToOnePeer;

import org.apache.log4j.Logger;


public class OneToOneTransaction extends AbstractOneToOnePeer {
    private final static Logger LOG = Logger.getLogger(OneToOneTransaction.class);

    public OneToOneTransaction(OneToOnePeer local) {
        super(local);
    }

    public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectPersistenceManager objectManager = NakedObjects.getPersistenceManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.clearAssociation(identifier, inObject, associate);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
                throw e;
            }
        } else {
            super.clearAssociation(identifier, inObject, associate);
        }
    }

    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectPersistenceManager objectManager = NakedObjects.getPersistenceManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.setAssociation(identifier, inObject, associate);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
               throw e;
            }
        } else {
            super.setAssociation(identifier, inObject, associate);
        }
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object value) {
        NakedObjectPersistenceManager objectManager = NakedObjects.getPersistenceManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.setValue(identifier, inObject, value);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
                throw e;
            }
        } else {
            super.setValue(identifier, inObject, value);
        }

    }
    

    private void abort(NakedObjectPersistenceManager objectManager) {
        LOG.info("exception executing " + getName() + ", aborting transaction");
        try {
            objectManager.abortTransaction();
        } catch (Exception e2) {
            LOG.error("failure during abort", e2);
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