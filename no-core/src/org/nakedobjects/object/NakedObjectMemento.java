package org.nakedobjects.object;

import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


public class NakedObjectMemento implements Serializable {

    private static class Data implements Serializable {
        private final static long serialVersionUID = 1L;
        String className;
        Object oid;

        public Data(Object oid, String className) {
            this.oid = oid;
            this.className = className;
        }

        public String toString() {
            return className + "/" + oid;
        }
    }

    private static class InternalCollectionData extends Data {
        private final static long serialVersionUID = 1L;
        Data[] elements;

        public InternalCollectionData(Object oid, String className, Data[] elements) {
            super(oid, className);
            this.elements = elements;
        }

        public String toString() {
            StringBuffer str = new StringBuffer("(");
            for (int i = 0; i < elements.length; i++) {
                str.append((i > 0) ? "," : "");
                str.append(elements[i]);
            }
            str.append(")");
            return str.toString();
        }
    }

    private static class ObjectData extends Data {
        private static class Null implements Serializable {
            private final static long serialVersionUID = 1L;

            public String toString() {
                return "NULL";
            }
        }

        private final static Serializable NO_ENTRY = new Null();
        private final static long serialVersionUID = 1L;
        private Hashtable fields = new Hashtable();

        public ObjectData(Object oid, String className) {
            super(oid, className);
        }

        public void addField(String fieldName, Object entry) {
            if (fields.containsKey(fieldName)) { throw new IllegalArgumentException("Field already entered " + fieldName); }
            fields.put(fieldName, entry == null ? NO_ENTRY : entry);
        }

        public Object getEntry(String fieldName) {
            Object entry = fields.get(fieldName);
            return entry == null || entry.getClass() == NO_ENTRY.getClass() ? null : entry;
        }

        public String toString() {
            return fields.toString();
        }
    }

    private static final Logger LOG = Logger.getLogger(NakedObjectMemento.class);
    private final static long serialVersionUID = 1L;
    private Data state;

    /**
     * Creates a memento that hold the state for the specified object. This object is Serializable
     * and can be passed over the network easily. Also for a persistent only the refernce Oids are
     * held, avoiding the need for serializing the whole object graph.
     */
    public NakedObjectMemento(NakedObject object) {
        state = object == null ? null : createData(object);
        LOG.debug("created memento for " + this);
    }

    private Data createData(NakedObject object) {
        if (object instanceof InternalCollection) {
            InternalCollection coll = (InternalCollection) object;
            Data[] collData = new Data[coll.size()];
            for (int j = 0; j < coll.size(); j++) {
                NakedObject ref = coll.elementAt(j);
                collData[j] = new Data(ref.getOid(), ref.getClassName());
            }

            return new InternalCollectionData(coll.getOid(), InternalCollection.class.getName(), collData);
        } else {
            return createObjectData(object);
        }
    }

    private ObjectData createObjectData(NakedObject object) {
        NakedClass cls = object.getNakedClass();
        ObjectData d = new ObjectData(object.getOid(), cls.fullName());

        Field[] fields = cls.getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.isDerived()) {
                if (field instanceof Value) {
                    NakedValue v = (NakedValue) field.get(object);
                    d.addField(field.getName(), v.isEmpty() ? null : v.saveString());
                } else if (field instanceof OneToManyAssociation) {
                    InternalCollection coll = (InternalCollection) field.get(object);
                    d.addField(field.getName(), createData(coll));
                } else if (field instanceof OneToOneAssociation) {
                    NakedObject ref = (NakedObject) field.get(object);
                    Object refOid = ref == null ? null : new Data(ref.getOid(), ref.getClassName());
                    d.addField(field.getName(), refOid);
                }
            }
        }
        return d;
    }

    public Object getOid() {
        return state.oid;
    }

    public NakedObject recreateObject(LoadedObjects objectManager) {
        if (state == null) {
            return null;
        } else {
            NakedClass nc = NakedObjectManager.getInstance().getNakedClass(state.className);
            NakedObject object = nc.acquireInstance();
            object.setOid(state.oid);
            LOG.debug("Recreated object " + object.getOid());
            updateNakedObject(object, objectManager);

            return object;
        }
    }

    private NakedObject recreateObject2(LoadedObjects objectManager, Data data) {
        synchronized (objectManager) {
            NakedObject ref;
            Object oid = data.oid;

            NakedClass nakedClass = NakedObjectManager.getInstance().getNakedClass(data.className);
            if (oid == null) {
                ref = null;
            } else if (objectManager.isLoaded(oid)) {
                ref = objectManager.getLoadedObject(oid);
            } else {
                ref = nakedClass.acquireInstance();
                ref.setOid(oid);
                objectManager.loaded(ref);
            }

            return ref;
        }
    }

    public String toString() {
        return "[" + (state == null ? null : state.className + "/" + state.oid + state) + "]";
    }

    /**
     * Updates the specified object (assuming it is the correct object for this memento) with the
     * state held by this memento.
     * 
     * @throws IllegalArgumentException
     *                    if the memento was created from different logical object to the one specified
     *                    (i.e. its oid differs).
     */
    public void updateNakedObject(NakedObject object, LoadedObjects objectManager) {
        Object oid = object.getOid();
        if (oid != null && !oid.equals(state.oid)) {
            throw new IllegalArgumentException("This memento can only be used to " + "update the naked object with the Oid "
                    + state.oid);

        } else {
            if (!(state instanceof ObjectData)) {
                throw new NakedObjectRuntimeException("Expected an ObjectData but got " + state.getClass());
            } else {
                ObjectData od = (ObjectData) state;

                Field[] fields = object.getNakedClass().getFields();

                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    Object fieldData = od.getEntry(field.getName());
                    if (!field.isDerived()) {
                        if (field instanceof Value) {
                            updateValue(object, field, (String) fieldData);
                        } else if (field instanceof OneToManyAssociation) {
                            updateOneToManyAssociation(object, objectManager, (OneToManyAssociation) field,
                                    (InternalCollectionData) fieldData);
                        } else if (field instanceof OneToOneAssociation) {
                            updateOneToOneAssociation(object, objectManager, (OneToOneAssociation) field, (Data) fieldData);
                        }
                    }
                }
            }
            LOG.debug("object updated " + object.getOid());
        }

    }

    private void updateOneToManyAssociation(NakedObject object, LoadedObjects objectManager, OneToManyAssociation field,
            InternalCollectionData collectionData) {
        InternalCollection collection = (InternalCollection) field.get(object);
        if (collection.getOid() == null) {
            collection.setOid(collectionData.oid);
        }

        Vector original = new Vector();
        int size = collection.size();
        for (int i = 0; i < size; i++) {
            original.addElement(collection.elementAt(i));
        }

        for (int j = 0; j < collectionData.elements.length; j++) {
            NakedObject element = recreateObject2(objectManager, (Data) collectionData.elements[j]);
            if (!collection.contains(element)) {
                LOG.debug("  association " + field + " changed, added " + element.getOid());
                collection.added(element);
            } else {
                original.removeElement(element);
            }
        }

        size = original.size();
        for (int i = 0; i < size; i++) {
            NakedObject element = (NakedObject) original.elementAt(i);
            LOG.debug("  association " + field + " changed, removed " + element.getOid());
            collection.remove(element);
        }
    }

    private void updateOneToOneAssociation(NakedObject object, LoadedObjects objectManager, OneToOneAssociation field,
            Data fieldData) {
        if (fieldData == null) {
            field.initData(object, null);
        } else {
            NakedObject ref = recreateObject2(objectManager, fieldData);
            if (field.get(object) != ref) {
                LOG.debug("  association " + field + " changed to " + ref.getOid());
                field.initData(object, ref);
            }
        }
    }

    private void updateValue(NakedObject object, Field field, String value) {
        NakedValue nv = ((NakedValue) field.get(object));

        if (value == null) {
            nv.clear();
        } else {
            LOG.debug("  value " + field + " changed to " + value);
            nv.restoreString(value);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
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