package org.nakedobjects.object.persistence;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.AbstractOneToOnePeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOnePeer;


public class OneToOneTransaction extends AbstractOneToOnePeer {

    public OneToOneTransaction(OneToOnePeer local) {
        super(local);
    }

    public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        try {
            if (inObject.isPersistent()) {
                objectManager.startTransaction();
            }

            super.clearAssociation(identifier, inObject, associate);
            objectManager.saveChanges();
            if (inObject.isPersistent()) {
                objectManager.endTransaction();
            }
        } catch (RuntimeException e) {
            objectManager.abortTransaction();
            throw e;
        }
    }

    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        try {
            if (inObject.isPersistent()) {
                objectManager.startTransaction();
            }
            super.setAssociation(identifier, inObject, associate);
            objectManager.saveChanges();
            if (inObject.isPersistent()) {
                objectManager.endTransaction();
            }
        } catch (RuntimeException e) {
            objectManager.abortTransaction();
            throw e;
        }
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object value) {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        try {
            if (inObject.isPersistent()) {
                objectManager.startTransaction();
            }
            super.setValue(identifier, inObject, value);
            objectManager.saveChanges();
            if (inObject.isPersistent()) {
                objectManager.endTransaction();
            }
        } catch (RuntimeException e) {
            objectManager.abortTransaction();
            throw e;
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