package org.nakedobjects.object.transaction;

import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectPersistenceManager;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.AbstractActionPeer;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.ReflectiveActionException;

import org.apache.log4j.Logger;

public class ActionTransaction extends AbstractActionPeer {
    private final static Logger LOG = Logger.getLogger(ActionTransaction.class);

    public ActionTransaction(ActionPeer decorated) {
        super(decorated);
    }
    
    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) throws ReflectiveActionException {
        NakedObjectPersistenceManager objectManager = NakedObjects.getPersistenceManager();

        try {
            objectManager.startTransaction();

            /*
             * TODO the object that we are invoking this method on, and the
             * parameters, need to be part of the transaction, and not the same
             * objects that other clients are using.
             */
            NakedObject transactionObject;
            Naked[] transactionParameters = new Naked[parameters == null ?0 : parameters.length];
           /* if (object.isPersistent()) {
                transactionObject = objectManager.getObject(object.getOid(), object.getSpecification());
                for (int i = 0; i < transactionParameters.length; i++) {
                    Naked parameter = (Naked) parameters[i];
                    if(parameter != null) {
                    Oid parameterOid = parameter == null ? null : parameter.getOid();
                    transactionParameters[i] = parameterOid == null ? parameter : objectManager.getObject(parameterOid, parameter
                            .getSpecification());
                    
                    	Assert.assertEquals(parameters[i], NakedObjects.getPojoAdapterFactory().createAdapter(transactionParameters[i].getObject()));
                    }
                }
            } else {
*/                // non-persistent
                transactionObject = object;
                transactionParameters = parameters == null ? new Naked[0] : parameters;
    //        }

            Naked result = super.execute(identifier, transactionObject, transactionParameters);

            objectManager.saveChanges();
            objectManager.endTransaction();

            return result;
        } catch (RuntimeException e) {
            LOG.info("exception executing " + getName() + "; aborting transaction");
            try {
                objectManager.abortTransaction();
            } catch (Exception e2) {
                LOG.error("failure during abort", e2);
            }
            throw e;
        }
    }



}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/