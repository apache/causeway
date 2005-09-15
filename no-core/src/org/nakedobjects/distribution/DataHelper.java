package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;


public class DataHelper {
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
            // create transient object
            NakedObject object;
            object = objectLoader.recreateTransientInstance(specification);
            object.setOptimisticLock(data.getVersion(), "", null);
            setUpFields(data, object, true);
            return object;
            
        } else if(objectLoader.isIdentityKnown(oid)) {
            // object known and we have all the latetest data; update the object
            NakedObject object;
            object =  objectLoader.getAdapterFor(oid);
            object.setOptimisticLock(data.getVersion(), "", null);
            if(data.getFieldContent() != null) {
	            ResolveState state = ResolveState.UPDATING;
	            if (object.getResolveState().isResolvable(state)) {
		            objectLoader.start(object, state);
		            setUpFields(data, object, data.isResolved());
		            objectLoader.end(object);
	            }
	            updateNotifier.addDirty(object);
            }
            return object;
        } else {
            // unknown object; create an instance
            NakedObject object;
            object = objectLoader.recreateAdapterForPersistent(oid, specification);
            object.setOptimisticLock(data.getVersion(), "", null);
            if(data.getFieldContent() != null) {
	            ResolveState state;
	            state = data.isResolved() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
	            if (object.getResolveState().isResolvable(state)) {
		            objectLoader.start(object, state);
		            setUpFields(data, object, data.isResolved());
		            objectLoader.end(object);
	            }
            }
            return object;
 	     }
    }

    private static void setUpFields(ObjectData data, NakedObject object, boolean completeData) {        
        Object[] fieldContent = data.getFieldContent();
        if (fieldContent != null && fieldContent.length > 0) {
            NakedObjectField[] fields = object.getSpecification().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fieldContent[i] instanceof NullData && ! completeData) {
                    /*
                     * Note - if fully resolving or updating the object then nulls should clear the
                     * field; if only part-resolving then nulls can be ignored as the fields are
                     * likely to be null at this point
                     */
                    continue;
                }
                
                if (fields[i].isCollection()) {
                    if (fieldContent[i] != null) {
                        /*
                         * TODO we need to be wary of the resolved state of the collection data - if
                         * the server collection is marked as resolved, but has no elements then the
                         * internal collection will be cleared out!
                         * 
                         * collection adapters should be initally marked as unresolved, and the OSes
                         * should mark them as resolved when they load in elements for them.
                         */
                        CollectionData collection = (CollectionData) fieldContent[i];
                        int size = collection.getElements().length;
                        // TODO remove this fudge
                        if(completeData || size > 0 ) {
	                        NakedObject[] instances = new NakedObject[size];
	                        for (int j = 0; j < instances.length; j++) {
	                            instances[j] = restoreObject(((ObjectData) collection.getElements()[j]));
	                        }
	                        object.initAssociation((OneToManyAssociation) fields[i], instances);
                        }
                    }
                } else if (fields[i].isValue()) {
                    if (fieldContent[i] != null) {
                        object.initValue((OneToOneAssociation) fields[i], fieldContent[i] instanceof NullData ? null
                                : ((ValueData) fieldContent[i]).getValue());
                    }
                } else {
                    if (fieldContent[i] != null) {
                        NakedObjectAssociation field = (NakedObjectAssociation) fields[i];
                        NakedObject associate;
                        if(fieldContent[i] instanceof NullData) {
                            associate = null;
                        } else {
                            associate = restoreObject((ObjectData) fieldContent[i]);
                        }
                        object.initAssociation(field, associate);
                    }
                }
            }
        }
    }

    private static Naked restoreCollection(CollectionData data) {
        Oid oid = data.getOid();
        String type = data.getType();
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(type);
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();

        /*
         * if we are to deal with internal collections then we need to be able to get the collection
         * from it's parent via its field
         */
        NakedCollection collection;
        collection = objectLoader.recreateCollection(specification);
        collection.setOid(oid);
        if (data.getElements() == null) {
            return collection;
        } else {
            ObjectData[] elements = data.getElements();
            Object[] initData = new Object[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NakedObject element = restoreObject(elements[i]);
                initData[i] = element.getObject();
            }
            collection.init(initData);
            return collection;
        }
    }

    private static Naked restoreValue(ValueData valueData) {
        Naked value = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
        return value;
    }

    
    
    /* ------------------------------------------------------------------------------ */
    
    /** @deprecated  use restore(ObjectData) */
    public static Naked recreate(Data data) {
        return restore(data);
    }

    /** @deprecated use restore(ObjectData)*/
    public static void resolve(ObjectData data, DirtyObjectSet updateNotifier) {
        restore(data);
    }

    /** @deprecated use restore(ObjectData) */
    public static void update(ObjectData data, DirtyObjectSet updateNotifier) {
        restore(data);
    }

    public static void setUpdateNotifer(DirtyObjectSet updateNotifier) {
        DataHelper.updateNotifier = updateNotifier;}
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