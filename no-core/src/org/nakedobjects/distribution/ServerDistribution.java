package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;


public class ServerDistribution implements ClientDistribution {
    private static final int OBJECT_DATA_DEPTH = 3;
    private LoadedObjects loadedObjects;
    private ObjectDataFactory objectDataFactory;
    private ObjectFactory objectFactory;
    private LocalObjectManager objectManager;

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        TypedNakedCollection instances = objectManager.allInstances(getSpecification(fullName), includeSubclasses);
        return convertToNakedCollection(instances);
    }

    public void clearAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        NakedObject associate = getNakedObject(session, associateOid, associateType);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(session, association, associate);
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

    public void destroyObject(Session session, Oid oid, String type) {
        NakedObject inObject = getNakedObject(session, oid, type);
        objectManager.destroyObject(inObject);
    }

    public ObjectData executeAction(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, ObjectData[] parameterData) {
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
            if (parameterData[i] != null) {
                parameters[i] = ObjectDataHelper.recreate(loadedObjects, parameterData[i]);
            }
        }
        NakedObject result = (NakedObject) object.execute(action, parameters);
        return objectDataFactory.createObjectData(result, OBJECT_DATA_DEPTH);
    }

    public ObjectData[] findInstances(Session session, String fullName, String criteria, boolean includeSubclasses) {
        TypedNakedCollection instances = objectManager.findInstances(getSpecification(fullName), criteria, includeSubclasses);
        return convertToNakedCollection(instances);
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, ObjectData[] parameters) {
        return new DefaultHint();
    }

    public NakedClass getNakedClass(String fullName) {
        return null;
    }

    private NakedObject getNakedObject(Session session, Oid oid, String fullName) {
        NakedObject object = objectManager.getObject(oid, getSpecification(fullName));
        return object;
    }

    public ObjectData getObject(Session session, Oid oid, String fullName) {
        NakedObject object = getNakedObject(session, oid, fullName);
        return objectDataFactory.createObjectData(object, OBJECT_DATA_DEPTH);
    }

    private NakedObjectSpecification getSpecification(String fullName) {
        return NakedObjects.getSpecificationLoader().loadSpecification(fullName);
    }

    public boolean hasInstances(Session session, String fullName) {
        return objectManager.hasInstances(getSpecification(fullName));
    }

    public Oid[] makePersistent(Session session, ObjectData data) {
        NakedObject object = ObjectDataHelper.recreate(loadedObjects, data);
        objectFactory.recreatedObject(object.getObject());
        objectManager.startTransaction();
        objectManager.makePersistent(object);
        objectManager.endTransaction();
        return new Oid[] { object.getOid() };
    }

    public int numberOfInstances(Session sessionId, String fullName) {
        return objectManager.numberOfInstances(getSpecification(fullName));
    }


    /**
     * .NET property
     * 
     * @property
     */
    public void set_LoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_LocalObjectManager(LocalObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectDataFactory(ObjectDataFactory objectDataFactory) {
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

    public void setAssociation(Session session, String fieldIdentifier, NakedObject inObject, NakedObject associate) {
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(session, association, associate);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        inObject.setAssociation(association, associate);
    }

    public void setAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        NakedObject associate = getNakedObject(session, associateOid, associateType);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(session, association, associate);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        inObject.setAssociation(association, associate);
    }

    public void setLoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    public void setLocalObjectManager(LocalObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setObjectDataFactory(ObjectDataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setValue(Session session, String fieldIdentifier, Oid objectOid, String objectType, Object value) {
        NakedObject inObject = getNakedObject(session, objectOid, objectType);
        OneToOneAssociation association = (OneToOneAssociation) inObject.getSpecification().getField(fieldIdentifier);
        Hint about = inObject.getHint(session, association, NakedObjects.getPojoAdapterFactory().createNOAdapter(value));
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
        inObject.setValue(association, value);
    }

    public void abortTransaction(Session session) {
        objectManager.abortTransaction();
    }

    public void endTransaction(Session session) {
        objectManager.endTransaction();
    }

    public void startTransaction(Session session) {
        objectManager.startTransaction();
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