package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
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

    public static Naked recreate(Data data) {
        if (data instanceof ValueData) {
            return recreateValue((ValueData) data);
        } else if (data instanceof NullData) {
            return null;
        } else {
            return recreateNaked((ObjectData) data);
        }
    }

    private static Naked recreateValue(ValueData valueData) {
        Naked value = NakedObjects.getObjectLoader().createAdapterForValue(valueData.getValue());
        return value;
    }

    public static Naked recreateNaked(ObjectData data) {
        Oid oid = data.getOid();
        String type = data.getType();
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(type);
        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();

        return recreateObject(data, oid, specification, objectLoader);
    }

    private static NakedObject recreateObject(
            ObjectData data,
            Oid oid,
            NakedObjectSpecification specification,
            NakedObjectLoader objectLoader) {
        NakedObject object;
        if (oid == null) {
            object = objectLoader.recreateTransientInstance(specification);
            object.setOptimisticLock(data.getVersion(), "", null);
            recreateObjectsInFields(data, object);
            return object;
        } else {
            object = objectLoader.recreateAdapterForPersistent(oid, specification);
            object.setOptimisticLock(data.getVersion(), "", null);

            ResolveState state;
            if (data.getFieldContent() == null) {
                return object;
            } else {
                state = data.isResolved() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;

                if (object.getResolveState().isResolvable(state)) {
                    objectLoader.start(object, state);
                    recreateObjectsInFields(data, object);
                    objectLoader.end(object);
                }
                return object;
            }
        }
    }

    private static void recreateObjectsInFields(ObjectData data, NakedObject object) {
        Object[] fieldContent = data.getFieldContent();
        boolean partResolving = object.getResolveState() == ResolveState.RESOLVING_PART;
        if (fieldContent != null && fieldContent.length > 0) {
            NakedObjectField[] fields = object.getSpecification().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fieldContent[i] instanceof NullData && partResolving) {
                    /*
                     * Note - if fully resolving or updating the object then nulls should
                     * clear the field; if only part-resolving then nulls can be ignored as
                     * the fields are likely to be null at this point
                     */
                    continue;
                }
                if (fields[i].isCollection()) {
                    if (fieldContent[i] != null) {
                        ObjectData collection = (ObjectData) fieldContent[i];
                        NakedObject[] instances = new NakedObject[collection.getFieldContent().length];
                        for (int j = 0; j < instances.length; j++) {
                            instances[j] = (NakedObject) recreateNaked(((ObjectData) collection.getFieldContent()[j]));
                        }
                        object.initOneToManyAssociation((OneToManyAssociation) fields[i], instances);
                    }
                } else if (fields[i].isValue()) {
                    if (fieldContent[i] != null) {
                        object.initValue((OneToOneAssociation) fields[i], ((ValueData) fieldContent[i]).getValue());
                    }
                } else {
                    if (fieldContent[i] != null) {
                        NakedObjectAssociation field = (NakedObjectAssociation) fields[i];
                        NakedObject associate = (NakedObject) recreate((Data) fieldContent[i]);
                        object.initAssociation(field, associate);
                    }
                }
            }
        }
    }

    public static void update(ObjectData data, DirtyObjectSet updateNotifier) {
        loadData(data, ResolveState.UPDATING, updateNotifier);
    }

    public static void resolve(ObjectData data, DirtyObjectSet updateNotifier) {
        loadData(data, ResolveState.RESOLVING, updateNotifier);
    }

    private static void loadData(ObjectData data, ResolveState initialState, DirtyObjectSet updateNotifier) {
        Oid oid = data.getOid();
        Object[] fieldContent = data.getFieldContent();
        long version = data.getVersion();

        NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();
        if (!objectLoader.isIdentityKnown(oid)) {
            // if we don't have an object in use then ignore the data about it.
            // TODO decide if we should recreate these objects, as they may be new objects that we
            // will get references to shortly
            return;
        } else {
            NakedObject object = updateExistingObject(oid, version, fieldContent, initialState, objectLoader);
            updateNotifier.addDirty(object);
        }
    }

    private static NakedObject updateExistingObject(
            Oid oid,
            long version,
            Object[] fieldContent,
            ResolveState initialState,
            NakedObjectLoader objectLoader) {
        NakedObject object;
        object = objectLoader.getAdapterFor(oid);
        object.setOptimisticLock(version, "", null);
        /*
         * note - we are not interested in the user and date - these are only used on the server
         * side when generating a concurrency exception
         */

        objectLoader.start(object, initialState);

        NakedObjectField[] fields = object.getSpecification().getFields();
        if (fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isCollection()) {
                    ObjectData collection = (ObjectData) fieldContent[i];
                    Object[] elements = collection.getFieldContent();
                    NakedObject[] instances = new NakedObject[elements.length];
                    for (int j = 0; j < instances.length; j++) {
                        instances[j] = (NakedObject) recreateNaked((ObjectData) elements[j]);
                    }
                    object.initOneToManyAssociation((OneToManyAssociation) fields[i], instances);

                } else if (fields[i].isValue()) {
                    object.initValue((OneToOneAssociation) fields[i], ((ValueData) fieldContent[i]).getValue());

                } else {
                    NakedObject field;
                    if (fieldContent[i] instanceof NullData) {
                        field = null;
                    } else {
                        field = (NakedObject) recreateNaked((ObjectData) fieldContent[i]);
                    }
                    object.initAssociation((NakedObjectAssociation) fields[i], field);
                }
            }
        }
        objectLoader.end(object);
        return object;
    }

    public static NakedObject recreateObject(ObjectData data) {
        return (NakedObject) recreateNaked(data);
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