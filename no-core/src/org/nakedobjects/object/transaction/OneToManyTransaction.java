package org.nakedobjects.object.transaction;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.reflect.AbstractOneToManyPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToManyPeer;

import org.apache.log4j.Logger;


public class OneToManyTransaction extends AbstractOneToManyPeer {
    private final static Logger LOG = Logger.getLogger(OneToManyTransaction.class);

    public OneToManyTransaction(OneToManyPeer peer) {
        super(peer);
    }

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.addAssociation(identifier, inObject, associate);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
                throw e;
            }
        } else {
            super.addAssociation(identifier, inObject, associate);
        }
    }

    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.removeAllAssociations(identifier, inObject);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
                throw e;
            }
        } else {
            super.removeAllAssociations(identifier, inObject);

        }
    }

    private void abort(NakedObjectManager objectManager) {
        LOG.info("exception executing " + getName() + ", aborting transaction");
        try {
            objectManager.abortTransaction();
        } catch (Exception e2) {
            LOG.error("failure during abort", e2);
        }
    }

    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        if (inObject.getResolveState().isPersistent()) {
            try {
                objectManager.startTransaction();
                super.removeAssociation(identifier, inObject, associate);
                objectManager.saveChanges();
                objectManager.endTransaction();
            } catch (RuntimeException e) {
                abort(objectManager);
                throw e;
            }
        } else {
            super.removeAssociation(identifier, inObject, associate);
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