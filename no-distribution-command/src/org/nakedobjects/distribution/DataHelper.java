package org.nakedobjects.distribution;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.ResolveState;

import org.apache.log4j.Logger;


public class DataHelper {
    private static final Logger LOG = Logger.getLogger(DataHelper.class);
    private static DirtyObjectSet updateNotifier;

    public static Naked restore(Data data) {
        if (data instanceof ValueData) {
            return restoreValue((ValueData) data);
        } else if (data instanceof NullData) {
            return null;
        } else if (data instanceof CollectionData) {
            return restoreCollection((CollectionData) data);
        } else {
            return restoreObject((ObjectData) data);
        }
    }
    
    private static NakedObject restoreObject(ObjectData data) {
        Oid oid = data.getOid();
        String type = data.getType();
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(type);
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();
        
        /*
         * either create a new transient object, get an existing object and update it if data is for
         * resolved object, or create new object and set it
         */ 
        if (oid == null) {
            NakedObject object;
            object = objectLoader.recreateTransientInstance(specification);
            LOG.debug("restore transient object " + object);
            setUpFields(data, object);
            return object;
            
        } else if(objectLoader.isIdentityKnown(oid)) {
            // object known and we have all the latetest data; update/resolve the object
            NakedObject object;
            object =  objectLoader.getAdapterFor(oid);
            if(data.getFieldContent() != null) {
                object.setOptimisticLock(data.getVersion());
	            ResolveState initialState = object.getResolveState();
                ResolveState state = null;
                if (initialState == ResolveState.RESOLVED) {
                    state = ResolveState.UPDATING;
                } else if (initialState == ResolveState.GHOST || initialState == ResolveState.PART_RESOLVED) {
                    state = data.hasCompleteData() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
                }
                if (state != null) {
	                LOG.debug("updating existing object (" + state.name() + ") " + object);
                    if (object.getResolveState().isResolvable(state)) {
                        objectLoader.start(object, state);
                        setUpFields(data, object);
                        objectLoader.end(object);
                    }
                    updateNotifier.addDirty(object);
	            }
            } else {
                if(data.getVersion().different(object.getVersion())) {
                    // TODO reload the object
                }

            }
            return object;
        } else {
            // unknown object; create an instance
            NakedObject object;
            object = objectLoader.recreateAdapterForPersistent(oid, specification);
            object.setOptimisticLock(data.getVersion());
            if(data.getFieldContent() != null) {
                ResolveState state;
	            state = data.hasCompleteData() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
                LOG.debug("restoring existing object (" + state.name() + ") " + object);
	            if (object.getResolveState().isResolvable(state)) {
		            objectLoader.start(object, state);
		            setUpFields(data, object);
		            objectLoader.end(object);
	            }
            } else {
                if(data.getVersion().different(object.getVersion())) {
                    // TODO reload the object if on cient; fail on server
                }

            }
            return object;
 	     }
    }

    private static void setUpFields(ObjectData data, NakedObject object) {
        Object[] fieldContent = data.getFieldContent();
        if (fieldContent != null && fieldContent.length > 0) {
            NakedObjectField[] fields = object.getSpecification().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fieldContent[i] == null || fields[i].isDerived()) {
                    LOG.debug("no data for field " + fields[i].getId());
                    continue;
                }

                if (fields[i].isCollection()) {
                    CollectionData collection = (CollectionData) fieldContent[i];
                    if (collection.hasAllElements()) {
                        int size = collection.getElements().length;
                        NakedObject[] elements = new NakedObject[size];
                        for (int j = 0; j < elements.length; j++) {
                            elements[j] = restoreObject(((ObjectData) collection.getElements()[j]));
                            LOG.debug("adding element to " + fields[i].getId() + ": " + elements[j]);
                        }

                        NakedCollection col = (NakedCollection) object.getField(fields[i]);
                        ResolveState initialState = col.getResolveState();
                        ResolveState state = null;
                        // TODO how do we deal with transient internal collections (as part a transient
                        // object)
                        if (initialState == ResolveState.RESOLVED) {
                            state = ResolveState.UPDATING;
                        } else if (initialState == ResolveState.GHOST || initialState == ResolveState.PART_RESOLVED) {
                            state = collection.hasAllElements() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
                        } else if (initialState == ResolveState.TRANSIENT) {
                            state = ResolveState.SERIALIZING_TRANSIENT;
                        }
                        if (state != null) {
                            NakedObjects.getObjectLoader().start(col, state);
                            object.initAssociation((OneToManyAssociation) fields[i], elements);
                            NakedObjects.getObjectLoader().end(col);
                        } else {
                            LOG.warn("not initialising collection " + col + " due to current state " + initialState);
                        }
                    }
                } else if (fields[i].isValue()) {
                    LOG.debug("setting value for field " + fields[i].getId() + ": " + fieldContent[i]);
                    object.initValue((OneToOneAssociation) fields[i], fieldContent[i] instanceof NullData ? null
                            : ((ValueData) fieldContent[i]).getValue());
                } else {
                    OneToOneAssociation field = (OneToOneAssociation) fields[i];
                    NakedObject associate;
                    if (fieldContent[i] instanceof NullData) {
                        associate = null;
                    } else {
                        associate = restoreObject((ObjectData) fieldContent[i]);
                    }
                    LOG.debug("setting association for field " + fields[i].getId() + ": " + associate);
                    object.initAssociation(field, associate);
                }
            }
        }
    }

    private static Naked restoreValue(ValueData valueData) {
        Naked value = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
        return value;
    }

    public static void setUpdateNotifer(DirtyObjectSet updateNotifier) {
        DataHelper.updateNotifier = updateNotifier;
    }

    private static Naked restoreCollection(CollectionData data) {
    //    Oid oid = data.getOid();
        String type = data.getType();
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(type);
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();
    
        /*
         * if we are to deal with internal collections then we need to be able to get the collection
         * from it's parent via its field
         */
        NakedCollection collection;
        collection = objectLoader.recreateCollection(specification);
      //  collection.setOid(oid);
        if (data.getElements() == null) {
            LOG.debug("restoring empty collection");
            return collection;
        } else {
            ObjectData[] elements = data.getElements();
            LOG.debug("restoring collection " + elements.length + " elements");
            Object[] initData = new Object[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NakedObject element = restoreObject(elements[i]);
                LOG.debug("restoring collection element :" + element);
                initData[i] = element.getObject();
            }
            collection.init(initData);
            return collection;
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
