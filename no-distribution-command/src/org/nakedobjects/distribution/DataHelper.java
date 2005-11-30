package org.nakedobjects.distribution;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.ResolveState;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public class DataHelper {
    private static final Logger LOG = Logger.getLogger(DataHelper.class);
    private static DirtyObjectSet updateNotifier;

    private static ResolveState nextState(ResolveState initialState, boolean complete) {
        ResolveState state = null;
        if (initialState == ResolveState.RESOLVED) {
            state = ResolveState.UPDATING;
        } else if (initialState == ResolveState.GHOST || initialState == ResolveState.PART_RESOLVED) {
            state = complete ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
        } else if (initialState == ResolveState.TRANSIENT) {
            state = ResolveState.SERIALIZING_TRANSIENT;
        }
        return state;
    }

    public static Naked restore(Data data) {
        if (data instanceof ValueData) {
            return restoreValue((ValueData) data);
        } else if (data instanceof NullData) {
            return null;
        } else if (data instanceof CollectionData) {
            return restoreCollection((CollectionData) data, new Hashtable());
        } else {
            return restoreObject((ObjectData) data, new Hashtable());
        }
    }

    private static Naked restoreCollection(CollectionData data, Hashtable previous) {
        String type = data.getType();
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(type);
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();

        /*
         * if we are to deal with internal collections then we need to be able to get the collection from it's
         * parent via its field
         */
        NakedCollection collection;
        collection = objectLoader.recreateCollection(specification);
        // collection.setOid(oid);
        if (data.getElements() == null) {
            LOG.debug("restoring empty collection");
            return collection;
        } else {
            ObjectData[] elements = data.getElements();
            LOG.debug("restoring collection " + elements.length + " elements");
            Object[] initData = new Object[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NakedObject element = restoreObject(elements[i], previous);
                LOG.debug("restoring collection element :" + element);
                initData[i] = element.getObject();
            }
            collection.init(initData);
            return collection;
        }
    }

    private static NakedObject restoreObject(ObjectData data, Hashtable previous) {
        if(previous.containsKey(data)) {
            return (NakedObject) previous.get(data);
        }
        
        Oid oid = data.getOid();
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();

        NakedObject object;
        /*
         * either create a new transient object, get an existing object and update it if data is for resolved
         * object, or create new object and set it
         */
        if (oid == null) {
            object = restoreTransient(data, objectLoader, previous);
        } else if (objectLoader.isIdentityKnown(oid)) {
            object = updateLoadedObject(data, oid, objectLoader, previous);
        } else {
            object = restorePersistentObject(data, oid, objectLoader, previous);
        }
        
        return object;
    }

    private static NakedObject restorePersistentObject(ObjectData data, Oid oid, NakedObjectLoader objectLoader, Hashtable previous) {
        // unknown object; create an instance
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(data.getType());

        NakedObject object;
        object = objectLoader.recreateAdapterForPersistent(oid, specification);
        if (data.getFieldContent() != null) {
            object.setOptimisticLock(data.getVersion());
            ResolveState state;
            state = data.hasCompleteData() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
            LOG.debug("restoring existing object (" + state.name() + ") " + object);
            setupFields(data, objectLoader, object, state, previous);
        }
        return object;
    }

    private static NakedObject restoreTransient(ObjectData data, NakedObjectLoader objectLoader, Hashtable previous) {
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(data.getType());

        NakedObject object;
        object = objectLoader.recreateTransientInstance(specification);
        LOG.debug("restore transient object " + object);
        previous.put(data, object);
        setUpFields(data, object, previous);
        return object;
    }

    private static NakedValue restoreValue(ValueData valueData) {
        NakedValue value = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
        return value;
    }

    public static void setUpdateNotifer(DirtyObjectSet updateNotifier) {
        DataHelper.updateNotifier = updateNotifier;
    }

    private static void setupFields(ObjectData data, NakedObjectLoader objectLoader, NakedObject object, ResolveState state, Hashtable previous) {
        if (object.getResolveState().isResolvable(state)) {
            objectLoader.start(object, state);
            setUpFields(data, object, previous);
            objectLoader.end(object);
        }
    }

    private static void setUpFields(ObjectData data, NakedObject object, Hashtable previous) {
        Data[] fieldContent = data.getFieldContent();
        if (fieldContent != null && fieldContent.length > 0) {
            NakedObjectField[] fields = object.getSpecification().getFields();
            for (int i = 0; i < fields.length; i++) {
                NakedObjectField field = fields[i];
                Data fieldData = fieldContent[i];
                if (fieldData == null || field.isDerived()) {
                    LOG.debug("no data for field " + field.getId());
                    continue;
                }

                if (field.isCollection()) {
                    setUpCollectionField(object, field, (CollectionData) fieldData, previous);
                } else if (field.isValue()) {
                    setUpValueField(object, field, fieldData);
                } else {
                    setUpReferenceField(object, field, fieldData, previous);
                }
            }
        }
    }

    private static void setUpReferenceField(NakedObject object, NakedObjectField field, Data data, Hashtable previous) {
        NakedObject associate;
        if (data instanceof NullData) {
            associate = null;
        } else {
            associate = restoreObject((ObjectData) data, previous);
        }
        LOG.debug("setting association for field " + field.getId() + ": " + associate);
        object.initAssociation(field, associate);
    }

    private static void setUpValueField(NakedObject object, NakedObjectField field, Data data) {
        Object value;
        if(data instanceof NullData) {
            value = null;
        } else {
            value = ((ValueData) data).getValue();
        }
        LOG.debug("setting value for field " + field.getId() + ": " + value);
        object.initValue((OneToOneAssociation) field, value);
    }

    private static void setUpCollectionField(NakedObject object, NakedObjectField field, CollectionData content, Hashtable previous) {
        if (!content.hasAllElements()) {
            return;
        }

        int size = content.getElements().length;
        NakedObject[] elements = new NakedObject[size];
        for (int j = 0; j < elements.length; j++) {
            elements[j] = restoreObject(((ObjectData) content.getElements()[j]), previous);
            LOG.debug("adding element to " + field.getId() + ": " + elements[j]);
        }

        NakedCollection col = (NakedCollection) object.getField(field);
        ResolveState initialState = col.getResolveState();
        ResolveState state = nextState(initialState, content.hasAllElements());
        if (state != null) {
            NakedObjects.getObjectLoader().start(col, state);
            object.initAssociation((OneToManyAssociation) field, elements);
            NakedObjects.getObjectLoader().end(col);
        } else {
            LOG.warn("not initialising collection " + col + " due to current state " + initialState);
        }
    }

    private static NakedObject updateLoadedObject(ObjectData data, Oid oid, NakedObjectLoader objectLoader, Hashtable previous) {
        // object known and we have all the latetest data; update/resolve the object
        NakedObject object;
        object = objectLoader.getAdapterFor(oid);
        if (data.getFieldContent() != null) {
            object.setOptimisticLock(data.getVersion());
            ResolveState state = nextState(object.getResolveState(), data.hasCompleteData());
            if (state != null) {
                LOG.debug("updating existing object (" + state.name() + ") " + object);
                setupFields(data, objectLoader, object, state, previous);
                updateNotifier.addDirty(object);
            }
        } else {
            if (data.getVersion() != null && data.getVersion().different(object.getVersion())) {
                // TODO reload the object
            }
        }
        return object;
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
