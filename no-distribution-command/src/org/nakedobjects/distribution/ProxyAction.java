package org.nakedobjects.distribution;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.reflect.AbstractActionPeer;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.ReflectiveActionException;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import org.apache.log4j.Logger;


public final class ProxyAction extends AbstractActionPeer {
    private final static Logger LOG = Logger.getLogger(ProxyAction.class);
    private Distribution connection;
    private final ObjectEncoder encoder;

    public ProxyAction(final ActionPeer local, final Distribution connection, ObjectEncoder encoder) {
        super(local);
        this.connection = connection;
        this.encoder = encoder;
    }

    public Naked execute(NakedObject target, Naked[] parameters) throws ReflectiveActionException {
        if (isToBeExecutedRemotely(target)) {
            return executeRemotely(target, parameters);
        } else {
            LOG.debug(debug("execute locally", getIdentifier(), target, parameters));
            return super.execute(target, parameters);
        }
    }

    private Naked executeRemotely(NakedObject target, Naked[] parameters) {
        Data[] parameterObjectData = parameterValues(parameters);
        LOG.debug(debug("execute remotely", getIdentifier(), target, parameters));
        ObjectData targetReference = encoder.createDataForActionTarget(target);
        ServerActionResultData result;
        try {
            String name = getIdentifier().getClassName() + "#" + getIdentifier().getName();
            result = connection.executeServerAction(NakedObjects.getCurrentSession(), getType().getName(), name,
                    targetReference, parameterObjectData);
        } catch (NakedObjectRuntimeException e) {
            LOG.error("remote exception: " + e.getMessage(), e);
            throw e;
        }

        // must deal with transient-now-persistent objects first
        madePersistent(target, result.getPersistedTarget());
        
        for (int i = 0; i < parameters.length; i++) {
            if(getParameterTypes()[i].isObject()) {
                madePersistent((NakedObject) parameters[i], result.getPersistedParameters()[i]);
            }
        }
        
        Data returned = result.getReturn();
        Naked returnedObject;
        returnedObject = returned instanceof NullData ? null : ObjectDecoder.restore(returned);

        ObjectData[] updates = result.getUpdates();
        for (int i = 0; i < updates.length; i++) {
            LOG.debug("update " + DistributionLogger.dump(updates[i]));
            ObjectDecoder.restore(updates[i]);
        }

        return returnedObject;
    }
    

    private void madePersistent(NakedObject object, ObjectData updates) {
        if(updates == null) {
            return;
        }

        if(object.getOid() == null && object.persistable() != Persistable.TRANSIENT) {
            NakedObjects.getObjectLoader().madePersistent(object, updates.getOid());
            object.setOptimisticLock(updates.getVersion());
        }

        Data[] fieldData = updates.getFieldContent();
        if(fieldData == null) {
            return;
        }
        NakedObjectField[] fields = object.getSpecification().getFields();
        for (int i = 0; i < fieldData.length; i++) {
            if(fieldData[i] == null) {
                continue;
            }
            if(fields[i].isObject()) {
                NakedObject field = object.getAssociation((OneToOneAssociation) fields[i]);
                ObjectData fieldContent = (ObjectData) updates.getFieldContent()[i];
                if(field != null) {
                    madePersistent(field, fieldContent);
                }
            } else if(fields[i].isCollection()) {
                CollectionData collectionData = (CollectionData) updates.getFieldContent()[i];
                for (int j = 0; j < collectionData.getElements().length; j++) {
                    NakedObject element = ((NakedCollection) object.getField(fields[i])).elementAt(j);
                    ObjectData elementData = collectionData.getElements()[j];
                    madePersistent(element, elementData);
                }
            }
        }
    }

    private boolean isToBeExecutedRemotely(NakedObject target) {
        boolean remoteOverride = getTarget() == Action.REMOTE;
        boolean localOverride = getTarget() == Action.LOCAL;

        if (localOverride) {
            return false;
        }

        if (remoteOverride) {
            return true;
        }
        
        if(target == null) {
            // for static methods there is no target
            return false;
        }

        boolean remoteAsPersistent = target.getOid() != null;
        return remoteAsPersistent;
    }

    private Data[] parameterValues(Naked[] parameters) {
        NakedObjectSpecification[] parameterTypes = getParameterTypes();
        return encoder.createDataForParameters(parameterTypes, parameters);
    }

    private String debug(String message, MemberIdentifier identifier, NakedObject target, Naked[] parameters) {
        if (LOG.isDebugEnabled()) {
            StringBuffer str = new StringBuffer();
            str.append(message);
            str.append(" ");
            str.append(identifier);
            str.append(" on ");
            str.append(target);
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    str.append(',');
                }
                str.append(parameters[i]);
            }
            return str.toString();
        } else {
            return "";
        }
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