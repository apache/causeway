package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
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
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;

import org.apache.log4j.Logger;


public class ServerDistribution implements Distribution {
    private static final Logger LOG = Logger.getLogger(ServerDistribution.class);
    private DataFactory objectDataFactory;
    private ObjectFactory objectFactory;

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        LOG.debug("request allInstances of " + fullName  + (includeSubclasses ? "(including subclasses)" : "") + " from " + session);
        TypedNakedCollection instances = objectManager().allInstances(getSpecification(fullName), includeSubclasses);
        return convertToNakedCollection(instances);
    }

    public void clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associated) {
        LOG.debug("request clearAssociation " + fieldIdentifier + " on " + target + " of " + associated+ " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        NakedObject associate = getPersistentNakedObject(session, associated);
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
            data[i] = objectDataFactory.createCompletePersistentGraph(instances.elementAt(i));
        }
        return data;
    }

    public void destroyObject(Session session, ReferenceData object) {
        LOG.debug("request destroyObject " + object+ " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, object);
        objectManager().destroyObject(inObject);
    }

    public Data executeAction(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameterData) {
        LOG.debug("request executeAction " + actionIdentifier + " on " + target + " for " + session);

        NakedObject object;// = getPersistentNakedObject(session, target);
        if (target instanceof ReferenceData && ((ReferenceData) target).getOid() != null) {
            object = getPersistentNakedObject(session, (ReferenceData) target);
        } else if (target instanceof ObjectData) {
            object = (NakedObject) DataHelper.recreate(target);
        } else {
            throw new NakedObjectRuntimeException();
        }
        
        Action action = getActionMethod(actionType, actionIdentifier, parameterData, object);
        checkHint(session, actionType, actionIdentifier, target, parameterData);
        Naked[] parameters = getParameters(session, parameterData);

        Naked result = object.execute(action, parameters);
        return objectDataFactory.createActionResult(result);
    }

    private Naked[] getParameters(Session session, Data[] parameterData) {
        Naked[] parameters = new Naked[parameterData.length];
        for (int i = 0; i < parameters.length; i++) {
            Data data = parameterData[i];
            if (data instanceof NullData) {
                continue;
            }
            
            if (data instanceof ReferenceData && ((ReferenceData) data).getOid() != null) {
                parameters[i] = getPersistentNakedObject(session, (ReferenceData) data);
            } else if (data instanceof ObjectData) {
                parameters[i] = DataHelper.recreate((ObjectData) data);
            } else if (data instanceof ValueData) {
                ValueData valueData = (ValueData) data;
                parameters[i] = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
            } else {
                throw new NakedObjectRuntimeException();
            }
        }
        return parameters;
    }

    private void checkHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameterData) {
        Hint about = getActionHint(session, actionType, actionIdentifier, target, parameterData);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
    }

    private Action getActionMethod(String actionType, String actionIdentifier, Data[] parameterData, NakedObject object) {
        NakedObjectSpecification[] parameterSpecifiactions = new NakedObjectSpecification[parameterData.length];
        for (int i = 0; i < parameterSpecifiactions.length; i++) {
            parameterSpecifiactions[i] = getSpecification(parameterData[i].getType());
        }
        Type type = Action.getType(actionType);
        Action action = (Action) object.getSpecification().getObjectAction(type, actionIdentifier, parameterSpecifiactions);
        return action;
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        LOG.debug("request findInstances " + criteria + " for " + session);
        TypedNakedCollection instances = objectManager().findInstances(criteria);
        return convertToNakedCollection(instances);
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        LOG.debug("request getActionHint " + actionIdentifier + " for " + session);
        return new DefaultHint();
    }

    private NakedObject getPersistentNakedObject(Session session, ReferenceData object) {
        LOG.debug("get object " + object + " for " + session);
        NakedObjectSpecification spec = getSpecification(object.getType());
        NakedObject obj = NakedObjects.getObjectManager().getObject(object.getOid(), spec);
        obj.checkLock(object.getVersion());
        return obj;
     }

    public ObjectData resolveImmediately(Session session, ReferenceData target) {
        LOG.debug("request resolveImmediately " + target +" for " + session);
         
        NakedObjectSpecification spec = getSpecification(target.getType());
        NakedObject object = NakedObjects.getObjectManager().getObject(target.getOid(), spec);

        return objectDataFactory.createCompletePersistentGraph(object);
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
        NakedObject object = (NakedObject) DataHelper.recreate(data);
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

    public void setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associated) {
        LOG.debug("request setAssociation " + fieldIdentifier + " on " +target + " with " + associated + " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        NakedObject associate = getPersistentNakedObject(session, associated);
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

    public void setValue(Session session, String fieldIdentifier, ReferenceData target, Object value) {
        LOG.debug("request setValue " + fieldIdentifier + " on " + target + " with " + value + " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        OneToOneAssociation association = (OneToOneAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(association, NakedObjects.getObjectLoader().createAdapterForValue(value));
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }

        NakedValue fieldValue = (NakedValue) inObject.getValue(association);
        if (fieldValue != null) {
            fieldValue.restoreFromEncodedString(((NakedValue) NakedObjects.getObjectLoader().createAdapterForValue(value))
                    .asEncodedString());
        }

        inObject.setValue(association, value);
    }

    public void abortTransaction(Session session) {
        LOG.debug("request abort transaction for " + session);
       objectManager().abortTransaction();
    }

    public void endTransaction(Session session) {
        LOG.debug("request end transaction for " + session);
        objectManager().endTransaction();
    }

    public void startTransaction(Session session) {
        LOG.debug("request start transaction for " + session);
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