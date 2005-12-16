package org.nakedobjects.distribution;

import org.nakedobjects.distribution.ObjectDecoder.KnownTransients;
import org.nakedobjects.object.Action;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.NullDirtyObjectSet;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import org.apache.log4j.Logger;


public class ServerDistribution implements Distribution {
    private static final Logger LOG = Logger.getLogger(ServerDistribution.class);
    private ObjectEncoder encoder;
    private SingleResponseUpdateNotifier updateNotifier;

    public ServerDistribution() {
        ObjectDecoder.setUpdateNotifer(new NullDirtyObjectSet());
    }

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        LOG.debug("request allInstances of " + fullName + (includeSubclasses ? "(including subclasses)" : "") + " from "
                + session);
        TypedNakedCollection instances = persistor().allInstances(getSpecification(fullName), includeSubclasses);
        return convertToNakedCollection(instances);
    }

    private void checkHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameterData) {
        Hint about = getActionHint(session, actionType, actionIdentifier, target, parameterData);
        if (about.canAccess().isVetoed() || about.canUse().isVetoed()) {
            throw new NakedObjectRuntimeException();
        }
    }

    public ObjectData[] clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associated) {
        LOG.debug("request clearAssociation " + fieldIdentifier + " on " + target + " of " + associated + " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        NakedObject associate = getPersistentNakedObject(session, associated);
        NakedObjectField association = (NakedObjectField) inObject.getSpecification().getField(fieldIdentifier);
        if (!association.isAuthorised() || association.isUsable(inObject).isVetoed()) {
            throw new IllegalRequestException("can't modify field as not visible or editable");
        }
        inObject.clearAssociation(association, associate);
        return getUpdates();
    }

    private ObjectData[] convertToNakedCollection(TypedNakedCollection instances) {
        ObjectData[] data = new ObjectData[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            data[i] = encoder.createCompletePersistentGraph(instances.elementAt(i));
        }
        return data;
    }

    public ServerActionResultData executeServerAction(
            Session session,
            String actionType,
            String actionIdentifier,
            ObjectData target,
            Data[] parameterData) {
        LOG.debug("request executeAction " + actionIdentifier + " on " + target + " for " + session);

        NakedObject object;
        if (target instanceof ReferenceData && ((ReferenceData) target).getOid() != null) {
            object = getPersistentNakedObject(session, (ReferenceData) target);
        } else if (target instanceof ObjectData) {
            object = (NakedObject) ObjectDecoder.restore(target);
        } else if (target == null) {
            object = null;
        } else {
            throw new NakedObjectRuntimeException();
        }

        Action action = getActionMethod(actionType, actionIdentifier, parameterData, object);
        checkHint(session, actionType, actionIdentifier, target, parameterData);
        Naked[] parameters = getParameters(session, parameterData);

        if (action == null) {
            throw new NakedObjectsRemoteException("Could not find method " + actionIdentifier);
        }

        Naked result = action.execute(object, parameters);// object.execute(action, parameters);

        ObjectData persistedTarget;
        if (target == null) {
            persistedTarget = null;
        } else {
            persistedTarget = encoder.createMadePersistentGraph(target, object, updateNotifier);
        }

        ObjectData[] persistedParameters = new ObjectData[parameterData.length];
        for (int i = 0; i < persistedParameters.length; i++) {
            if (action.getParameterTypes()[i].isObject() && parameterData[i] instanceof ObjectData) {
                persistedParameters[i] = encoder.createMadePersistentGraph((ObjectData) parameterData[i],
                        (NakedObject) parameters[i], updateNotifier);
            }
        }
        // TODO find messages/warnings
        String[] messages = new String[0];
        String[] warnings = new String[0];

        // TODO for efficiency, need to remove the objects in the results graph from the updates set
        return encoder.createActionResult(result, getUpdates(), persistedTarget, persistedParameters, messages, warnings);
    }

    public ClientActionResultData executeClientAction(
            Session session,
            ObjectData[] persisted,
            ObjectData[] changed,
            ReferenceData[] deleted) {
        LOG.debug("execute client action for " + session);
        LOG.debug("start transaction");
        NakedObjectPersistor persistor = persistor();
        persistor.startTransaction();
        try {
            KnownTransients knownObjects = ObjectDecoder.createKnownTransients();
            NakedObject[] persistedObjects = new NakedObject[persisted.length];
            for (int i = 0; i < persisted.length; i++) {
                LOG.debug("  makePersistent " + persisted[i]);
                NakedObject object = (NakedObject) ObjectDecoder.restore(persisted[i], knownObjects);
                persistor.makePersistent(object);
                persistedObjects[i] = object;
            }
            NakedObject[] changedObjects = new NakedObject[changed.length];
            for (int i = 0; i < changed.length; i++) {
                LOG.debug("  objectChanged " + changed[i]);
                NakedObject object = (NakedObject) ObjectDecoder.restore(changed[i], knownObjects);
                persistor.objectChanged(object);
                changedObjects[i] = object;
            }
            for (int i = 0; i < deleted.length; i++) {
                LOG.debug("  destroyObject " + deleted[i] + " for " + session);
                NakedObject inObject = getPersistentNakedObject(session, deleted[i]);
                persistor.destroyObject(inObject);
            }
            LOG.debug("  end transaction");
            persistor.endTransaction();

            ObjectData[] madePersistent = new ObjectData[persisted.length];
            for (int i = 0; i < persisted.length; i++) {
                madePersistent[i] = encoder.createMadePersistentGraph(persisted[i], persistedObjects[i], updateNotifier);
            }
            Version[] changedVersion = new Version[changed.length];
            for (int i = 0; i < changed.length; i++) {
                changedVersion[i] = changedObjects[i].getVersion();
            }
            return encoder.createClientActionResultData(madePersistent, changedVersion);
        } catch (RuntimeException e) {
            LOG.debug("abort transaction", e);
            persistor.abortTransaction();
            throw e;
        }
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        LOG.debug("request findInstances " + criteria + " for " + session);
        TypedNakedCollection instances = persistor().findInstances(criteria);
        return convertToNakedCollection(instances);
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        LOG.debug("request getActionHint " + actionIdentifier + " for " + session);
        return new DefaultHint();
    }

    private Action getActionMethod(String actionType, String actionIdentifier, Data[] parameterData, NakedObject object) {
        NakedObjectSpecification[] parameterSpecifiactions = new NakedObjectSpecification[parameterData.length];
        for (int i = 0; i < parameterSpecifiactions.length; i++) {
            parameterSpecifiactions[i] = getSpecification(parameterData[i].getType());
        }

        Action.Type type = ActionImpl.getType(actionType);

        int pos = actionIdentifier.indexOf('#');
        String className = actionIdentifier.substring(0, pos);
        String methodName = actionIdentifier.substring(pos + 1);

        Action action;
        if (object == null) {
            action = (Action) NakedObjects.getSpecificationLoader().loadSpecification(className).getClassAction(type, methodName,
                    parameterSpecifiactions);
        } else {
            action = (Action) object.getSpecification().getObjectAction(type, methodName, parameterSpecifiactions);
        }
        return action;
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
                parameters[i] = ObjectDecoder.restore((ObjectData) data);
            } else if (data instanceof ValueData) {
                ValueData valueData = (ValueData) data;
                parameters[i] = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
            } else {
                throw new NakedObjectRuntimeException();
            }
        }
        return parameters;
    }

    private NakedObject getPersistentNakedObject(Session session, ReferenceData object) {
        NakedObjectSpecification spec = getSpecification(object.getType());
        NakedObject obj = NakedObjects.getObjectPersistor().getObject(object.getOid(), spec);
        LOG.debug("get object " + object + " for " + session + " --> " + obj);
        obj.checkLock(object.getVersion());
        return obj;
    }

    private NakedObjectSpecification getSpecification(String fullName) {
        return NakedObjects.getSpecificationLoader().loadSpecification(fullName);
    }

    private ObjectData[] getUpdates() {
        NakedObject[] updateObjects = updateNotifier.getUpdates();
        int noUpdates = updateObjects.length;
        ObjectData[] updateData = new ObjectData[noUpdates];
        for (int i = 0; i < noUpdates; i++) {
            ObjectData objectData = encoder.createForUpdate(updateObjects[i]);
            updateData[i] = objectData;
        }
        return updateData;
    }

    public boolean hasInstances(Session session, String objectType) {
        LOG.debug("request hasInstances of " + objectType + " for " + session);
        return persistor().hasInstances(getSpecification(objectType), false);
    }

    public int numberOfInstances(Session session, String objectType) {
        LOG.debug("request numberOfInstances of " + objectType + " for " + session);
        return persistor().numberOfInstances(getSpecification(objectType), false);
    }

    private NakedObjectPersistor persistor() {
        return NakedObjects.getObjectPersistor();
    }

    public Data resolveField(Session session, ReferenceData target, String fieldName) {
        LOG.debug("request resolveEagerly " + target + "/" + fieldName + " for " + session);

        NakedObjectSpecification spec = getSpecification(target.getType());
        NakedObjectField field = spec.getField(fieldName);
        // NakedObject object = NakedObjects.getObjectManager().getObject(target.getOid(), spec);
        NakedObject object = NakedObjects.getObjectLoader().recreateAdapterForPersistent(target.getOid(), spec);
        NakedObjects.getObjectPersistor().resolveField(object, field);
        return encoder.createForResolveField(object, fieldName);
    }

    public ObjectData resolveImmediately(Session session, ReferenceData target) {
        LOG.debug("request resolveImmediately " + target + " for " + session);

        NakedObjectSpecification spec = getSpecification(target.getType());
        NakedObject object = NakedObjects.getObjectPersistor().getObject(target.getOid(), spec);

        return encoder.createCompletePersistentGraph(object);
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Encoder(ObjectEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * .NET property
     * 
     * @property
     * 
     */
    public void set_UpdateNotifier(SingleResponseUpdateNotifier updateNotifier) {
        setUpdateNotifier(updateNotifier);
    }

    public ObjectData[] setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associated) {
        LOG.debug("request setAssociation " + fieldIdentifier + " on " + target + " with " + associated + " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        NakedObject associate = getPersistentNakedObject(session, associated);
        NakedObjectField association = (NakedObjectField) inObject.getSpecification().getField(fieldIdentifier);
        if (!association.isAuthorised() || association.isUsable(inObject).isVetoed()) {
            throw new IllegalRequestException("can't modify field as not visible or editable");
        }
        inObject.setAssociation(association, associate);
        return getUpdates();
    }

    /*
     * public void setLocalObjectManager(LocalObjectManager objectManager) { this.objectManager =
     * objectManager; }
     */
    public void setEncoder(ObjectEncoder objectDataFactory) {
        this.encoder = objectDataFactory;
    }

    public void setUpdateNotifier(SingleResponseUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    public ObjectData[] setValue(Session session, String fieldIdentifier, ReferenceData target, Object value) {
        LOG.debug("request setValue " + fieldIdentifier + " on " + target + " with " + value + " for " + session);
        NakedObject inObject = getPersistentNakedObject(session, target);
        OneToOneAssociation association = (OneToOneAssociation) inObject.getSpecification().getField(fieldIdentifier);
        if (!association.isAuthorised() || association.isUsable(inObject).isVetoed()) {
            throw new IllegalRequestException("can't modify field as not visible or editable");
        }

        NakedValue fieldValue = (NakedValue) inObject.getValue(association);
        if (fieldValue != null) {
            fieldValue.restoreFromEncodedString(((NakedValue) NakedObjects.getObjectLoader().createAdapterForValue(value))
                    .asEncodedString());
        }

        inObject.setValue(association, value);
        return getUpdates();
    }

    public String updateList() {
        return updateNotifier.updateList();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */