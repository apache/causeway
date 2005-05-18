package org.nakedobjects.object.persistence;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.AbstractActionPeer;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.ReflectiveActionException;

import org.apache.log4j.Category;

public class ActionTransaction extends AbstractActionPeer {
    private final static Category LOG = Category.getInstance(ActionTransaction.class);

    public ActionTransaction(ActionPeer decorated) {
        super(decorated);
    }
    
    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) throws ReflectiveActionException {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();

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

 /*       } catch (ObjectNotFoundException e) {
            LOG.error("Non-existing target or parameter used in " + getName(), e);
            objectManager.abortTransaction();
            return null;*/
        } catch (RuntimeException e) {
            LOG.error("Exception executing " + getName(), e);
            objectManager.abortTransaction();
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