package org.nakedobjects.distribution.server;

import org.nakedobjects.distribution.ActionType;
import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.HintData;
import org.nakedobjects.distribution.InstanceSet;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.ParameterSet;
import org.nakedobjects.distribution.RemoteException;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Action.Type;


public class RemoteMechanism implements DistributionInterface{
    private LoadedObjects loadedObjects;
    private NakedObjectManager objectManager;
    private RemoteObjectFactory factory;
  
    
    public void setFactory(RemoteObjectFactory factory) {
        this.factory = factory;
    }

    public HintData valueHint(final SessionId token, final ObjectReference reference, final String fieldName) {
        NakedObject inObject = getObject(reference);
        OneToOneAssociation value = (OneToOneAssociation) inObject.getSpecification().getField(fieldName);
        Hint about = inObject.getHint(token.getSession(), value, null);
        return factory.createAboutData(about);
    }

    public HintData actionHint(SessionId securityToken, ObjectReference target, final ActionType actionType, String actionName, ParameterSet parameterSet) {
        NakedObject object = target.getObject(objectManager);
        Naked[] parameters = parameterSet.recreateParameters(object.getContext());
        NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[parameters.length];
        for (int i = 0; i < parameterClasses.length; i++) {
            parameterClasses[i] = parameters[i].getSpecification();
        }
        Type type = actionType.getType();
        Action action = (Action) object.getSpecification().getObjectAction(type, actionName, parameterClasses);
        Hint about = object.getHint(securityToken.getSession(), action, null);
        return factory.createAboutData(about);
    }

    private NakedObject getObject(final ObjectReference reference) {
        return reference.getObject(objectManager);
    }
    
    public void associateObject(final SessionId token, final ObjectReference target, final String fieldName, final ObjectReference associate) {
        NakedObject inObject = getObject(target);
        NakedObject object = getObject(associate);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldName);
        inObject.setAssociation(association, object);
    }

    public void destroyObject(final SessionId token, final ObjectReference target) {
        NakedObject object = getObject(target);
        objectManager.destroyObject(object);
    }

    public void dissociateObject(final SessionId token, final ObjectReference target, final String fieldName, final ObjectReference associate) {
        NakedObject inObject = getObject(target);
        NakedObject object = getObject(associate);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldName);
        inObject.setAssociation(association, object);
    }

    public ObjectData executeAction(final SessionId token, final ObjectReference target, final ActionType actionType, final String actionName, ParameterSet parameterSet) {
        NakedObject object = target.getObject(objectManager);
        Naked[] parameters = parameterSet.recreateParameters(object.getContext());
        NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[parameters.length];
        for (int i = 0; i < parameterClasses.length; i++) {
            parameterClasses[i] = parameters[i].getSpecification();
        }
        Type type = actionType.getType();
        Action action = (Action) object.getSpecification().getObjectAction(type, actionName, parameterClasses);
        NakedObject result = object.execute(action, parameters);
        return factory.createObjectData(result);
    }

    public ObjectData getAssociation(final SessionId token, final ObjectReference target, final String fieldName) {
        NakedObject inObject = getObject(target);
        NakedObjectAssociation association = (NakedObjectAssociation) inObject.getSpecification().getField(fieldName);
        NakedObject object = inObject.getField(association);
        return factory.createObjectData(object);
    }

    public InstanceSet findInstances(final SessionId token, final String classReference, final String criteria) throws RemoteException {
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(classReference);
        try {
            TypedNakedCollection instances = objectManager.findInstances(cls, criteria, true);
            return factory.createInstancesSet(instances);
        } catch (UnsupportedFindException e) {
            throw new RemoteException(e);
        }
    }

    public InstanceSet findInstances(final SessionId token, final ObjectData pattern) throws RemoteException {
        TypedNakedCollection instances;
        try {
            // TODO pass in context
            instances = objectManager.findInstances(pattern.recreateObject(loadedObjects, null), true);
	        return factory.createInstancesSet(instances);
        } catch (UnsupportedFindException e) {
            throw new RemoteException(e);
        }
    }

    public InstanceSet allInstances(final SessionId token, final String classReference, boolean includeSubclasses) {
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(classReference);
         TypedNakedCollection instances = objectManager.allInstances(cls, includeSubclasses);
         return factory.createInstancesSet(instances);
    }

    public ObjectData getObjectRequest(final SessionId token, final ObjectReference reference) throws RemoteException {
        NakedObject object;
        object = reference.getObject(objectManager);
        return factory.createObjectData(object);
    }

    public boolean hasInstances(final SessionId token, final String classReference) {
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(classReference);
        return objectManager.hasInstances(cls);
    }
 
    public Oid makePersistentRequest(final SessionId token, final ObjectData object) {
        // TODO pass in context
        NakedObject recreatedObject = object.recreateObject(loadedObjects, null);
        objectManager.makePersistent(recreatedObject);
        /* TODO what happens if a set of objects are made persistent then all the newly persitent
         objects need to be have their new OIDs passed back */
        return recreatedObject.getOid();
    }

    public int numberOfInstances(final SessionId token, final String classReference) {
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(classReference);
        return objectManager.numberOfInstances(cls);
    }

    public ObjectData resolve(SessionId securityToken, ObjectReference reference) {
        NakedObject object = reference.getObject(objectManager);
        objectManager.resolve(object);
        return factory.createObjectData(object);
    }

    public void saveValue(final SessionId token, final ObjectReference target, final String fieldName, final String encodedValue) throws RemoteException {
     // try {
	        NakedObject inObject = getObject(target);
	        OneToOneAssociation value = (OneToOneAssociation) inObject.getSpecification().getField(fieldName);
            inObject.setValue(value, encodedValue);
       /* } catch (InvalidEntryException e) {
            throw new RemoteException(e);
        }*/
    }

    public void clearValue(final SessionId token, final ObjectReference target, final String fieldName) {
        NakedObject inObject = getObject(target);
        OneToOneAssociation value = (OneToOneAssociation) inObject.getSpecification().getField(fieldName);
        inObject.clear(value);
    }

    public long serialNumber(final SessionId token, String name) {
        return objectManager.serialNumber(name);
    }

    public void setLoadedObjects(final LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    public void setObjectManager(final NakedObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public NakedClass getNakedClass(String name) {
        NakedObjectSpecification nakedClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(name);
        return objectManager.getNakedClass(nakedClass);
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