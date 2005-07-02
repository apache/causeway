package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;


public class ServerDistribution implements ClientDistribution {
    private static final Logger LOG = Logger.getLogger(ServerDistribution.class);
    private static final int OBJECT_DATA_DEPTH = 3;
    private DataFactory objectDataFactory;
    private ObjectFactory objectFactory;

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        LOG.debug("request allInstances of " + fullName  + (includeSubclasses ? "(including subclasses)" : "") + " from " + session);
        TypedNakedCollection instances = objectManager().allInstances(getSpecification(fullName), includeSubclasses);
        return convertToNakedCollection(instances);
    }

    public void clearAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        LOG.debug("request clearAssociation " + fieldIdentifier + " on " + objectType + "/" + objectOid + " of " + associateType + "/" + associateOid + " for " + session);
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        NakedObject associate = getNakedObject(session, associateOid, associateType);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(association, associate);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        inObject.clearAssociation(association, associate);
    }

    private ObjectData[] convertToNakedCollection(TypedNakedCollection instances) {
        ObjectData[] data = new ObjectData[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            data[i] = objectDataFactory.createObjectData(instances.elementAt(i), OBJECT_DATA_DEPTH);
        }
        return data;
    }

    public void destroyObject(Session session, Oid oid, String objectType) {
        LOG.debug("request destroyObject " + objectType + "/" + oid + " for " + session);
        NakedObject inObject = getNakedObject(session, oid, objectType);
        objectManager().destroyObject(inObject);
    }

    public Data executeAction(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameterData) {
        LOG.debug("request executeAction " + actionIdentifier + " on " + objectType + "/" + objectOid + " for " + session);

        NakedObject object = getNakedObject(session, objectOid, objectType);

        NakedObjectSpecification[] parameterSpecifiactions = new NakedObjectSpecification[parameterTypes.length];
        for (int i = 0; i < parameterSpecifiactions.length; i++) {
            parameterSpecifiactions[i] = getSpecification(parameterTypes[i]);
        }
        Type type = Action.getType(actionType);
        Action action = (Action) object.getSpecification().getObjectAction(type, actionIdentifier, parameterSpecifiactions);

        Hint about = getActionHint(session, actionType, actionIdentifier, parameterTypes, objectOid, objectType, parameterData);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        Naked[] parameters = new Naked[parameterData.length];
        for (int i = 0; i < parameters.length; i++) {
            Data data = parameterData[i];
            if (data == null) {
                continue;
            }
            
            if (data instanceof ObjectData) {
                ObjectData objectData = (ObjectData) data;
                if (objectData.getOid() != null) {
                    parameters[i] = getNakedObject(session, objectData.getOid(), objectData.getType());
                    Assert.assertEquals(parameters[i], NakedObjects.getPojoAdapterFactory().createAdapter(parameters[i].getObject()));
                } else {
                    parameters[i] = DataHelper.recreate(data);
                }
            } else if (data instanceof ValueData) {
                ValueData valueData = (ValueData) data;
                parameters[i] = NakedObjects.getPojoAdapterFactory().createAdapter(valueData.getValue());
            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        // TEMP - TO BE REMOVED
        ((LocalObjectManager) objectManager()).tempResetDirty();

        try {
            NakedObject result = (NakedObject) object.execute(action, parameters);
            return objectDataFactory.createObjectData(result, OBJECT_DATA_DEPTH);
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            
            String exceptionType = e.getClass().getName();
            String message = e.getMessage();
            String trace;
            
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(baos));
                trace = baos.toString();
                baos.close();
            } catch (IOException ex) {
                LOG.error(ex);
                trace = "failed to get trace - see log";
            }

            return objectDataFactory.createExceptionData(exceptionType, message, trace);
        }
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        LOG.debug("request findInstances " + criteria + " for " + session);
        TypedNakedCollection instances = objectManager().findInstances(criteria);
        return convertToNakedCollection(instances);
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameters) {
        LOG.debug("request getActionHint " + actionIdentifier + " for " + session);
        return new DefaultHint();
    }

    public NakedClass getNakedClass(String fullName) {
        return null;
    }

    private NakedObject getNakedObject(Session session, Oid oid, String objectType) {
        LOG.debug("request getNakedObject " + objectType + "/" + oid + " for " + session);
        NakedObject object;
        try {
            object = objectManager().getObject(oid, getSpecification(objectType));
        } catch (ObjectNotFoundException e) {
            throw new NakedObjectRuntimeException(e);
        }
        return object;
    }

    public ObjectData resolveImmediately(Session session, Oid oid, String objectType) {
        LOG.debug("request resolveImmediately " + objectType + "/" + oid +" for " + session);
        NakedObject object = getNakedObject(session, oid, objectType);
        return objectDataFactory.createObjectData(object, OBJECT_DATA_DEPTH);
    }

    private NakedObjectSpecification getSpecification(String fullName) {
        return NakedObjects.getSpecificationLoader().loadSpecification(fullName);
    }

    public boolean hasInstances(Session session, String objectType) {
        LOG.debug("request hasInstances of " +  objectType + " for " + session);
        return objectManager().hasInstances(getSpecification(objectType));
    }

    public Oid[] makePersistent(Session session, ObjectData data) {
        LOG.debug("request makePersistent " + data +  " for " + session);
        NakedObject object = DataHelper.recreateObject(data);
        objectFactory.initRecreatedObject(object.getObject());
        objectManager().startTransaction();
        objectManager().makePersistent(object);
        objectManager().endTransaction();
        return new Oid[] { object.getOid() };
    }

    public int numberOfInstances(Session session, String objectType) {
        LOG.debug("request numberOfInstances of " + objectType + " for " + session);
        return objectManager().numberOfInstances(getSpecification(objectType));
    }

    /**
     * .NET property
     * 
     * @property / public void set_LocalObjectManager(LocalObjectManager
     *                       objectManager) { this.objectManager = objectManager; }
     */

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        LOG.debug("request setAssociation " + fieldIdentifier + " on " + objectType + "/" + objectOid + " with " + associateType + "/" + associateOid+ " for " + session);
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        NakedObject associate = getNakedObject(session, associateOid, associateType);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(association, associate);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        inObject.setAssociation(association, associate);
    }

    /*
     * public void setLocalObjectManager(LocalObjectManager objectManager) {
     * this.objectManager = objectManager; }
     */
    public void setObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setValue(Session session, String fieldIdentifier, Oid objectOid, String objectType, Object value) {
        LOG.debug("request setValue " + fieldIdentifier + " on " + objectType + "/" + objectOid + " with " + value + " for " + session);
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        OneToOneAssociation association = (OneToOneAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(association, NakedObjects.getPojoAdapterFactory().createAdapter(value));
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }

        NakedValue fieldValue = (NakedValue) inObject.getValue(association);
        if (fieldValue != null) {
            fieldValue.restoreFromEncodedString(((NakedValue) NakedObjects.getPojoAdapterFactory().createAdapter(value))
                    .asEncodedString());
        }

        inObject.setValue(association, value);
    }

    public void abortTransaction(Session session) {
        objectManager().abortTransaction();
    }

    public void endTransaction(Session session) {
        objectManager().endTransaction();
    }

    public void startTransaction(Session session) {
        objectManager().startTransaction();
    }

    private NakedObjectManager objectManager() {
        return NakedObjects.getObjectManager();
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