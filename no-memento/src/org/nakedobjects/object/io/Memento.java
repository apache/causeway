package org.nakedobjects.object.io;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;


public class Memento implements Transferable, Serializable {
    private final static long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Memento.class);
    private Data state;

    /**
     * Creates a memento that hold the state for the specified object. This
     * object is Serializable and can be passed over the network easily. Also
     * for a persistent only the refernce Oids are held, avoiding the need for
     * serializing the whole object graph.
     */
    public Memento(NakedObject object) {
        state = object == null ? null : createData(object);
        LOG.debug("created memento for " + this);
    }

    public Memento() {}

    private Data createData(Naked object) {
        if (object instanceof InternalCollection) {
            InternalCollection coll = (InternalCollection) object;
            Data[] collData = new Data[coll.size()];
            for (int j = 0; j < coll.size(); j++) {
                NakedObject ref = coll.elementAt(j);
                collData[j] = new Data(ref.getOid(), ref.getSpecification().getFullName());
            }

            return new InternalCollectionData(coll.getOid(), InternalCollection.class.getName(), collData);
        } else {
            return createObjectData((NakedObject) object);
        }
    }

    private ObjectData createObjectData(NakedObject object) {
        NakedObjectSpecification cls = object.getSpecification();
        ObjectData d = new ObjectData(object.getOid(), cls.getFullName());

        NakedObjectField[] fields = cls.getFields();

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            if (!field.isDerived()) {
               if (field instanceof OneToManyAssociation) {
                    InternalCollection coll = (InternalCollection) object.getField(field);
                    d.addField(field.getName(), createData(coll));
                } else if (field instanceof OneToOneAssociation) {
                    NakedObject ref = object.getAssociation((OneToOneAssociation) field);
                    Object refOid = ref == null ? null : new Data(ref.getOid(), ref.getSpecification().getFullName());
                    d.addField(field.getName(), refOid);
                }
            }
        }
        return d;
    }

    public Object getOid() {
        return state.oid;
    }

    public NakedObject recreateObject(LoadedObjects loadedObjects, NakedObjectContext context) {
        if (state == null) {
            return null;
        } else {
            NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(state.className);
            NakedObject object = (NakedObject) nc.acquireInstance();
            object.setContext(context);
            object.setOid(state.oid);
            LOG.debug("Recreated object " + object.getOid());
            updateObject(object, loadedObjects, context);

            return object;
        }
    }

    private NakedObject recreateObject2(LoadedObjects loadedObjects, Data data, NakedObjectContext context) {
        synchronized (loadedObjects) {
            NakedObject ref;
            Oid oid = data.oid;

            NakedObjectSpecification nakedClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(data.className);
            if (oid == null) {
                ref = null;
            } else if (loadedObjects.isLoaded(oid)) {
                ref = loadedObjects.getLoadedObject(oid);
            } else {
                ref = (NakedObject) nakedClass.acquireInstance();
                ref.setContext(context);
                ref.setOid(oid);
                loadedObjects.loaded(ref);
            }

            return ref;
        }
    }

    public String toString() {
        return "[" + (state == null ? null : state.className + "/" + state.oid + state) + "]";
    }

    /**
     * Updates the specified object (assuming it is the correct object for this
     * memento) with the state held by this memento.
     * 
     * @throws IllegalArgumentException
     *                       if the memento was created from different logical object to
     *                       the one specified (i.e. its oid differs).
     */
    public void updateObject(NakedObject object, LoadedObjects loadedObjects, NakedObjectContext context) {
        Object oid = object.getOid();
        if (oid != null && !oid.equals(state.oid)) {
            throw new IllegalArgumentException("This memento can only be used to " + "update the naked object with the Oid "
                    + state.oid);

        } else {
            if (!(state instanceof ObjectData)) {
                throw new NakedObjectRuntimeException("Expected an ObjectData but got " + state.getClass());
            } else {
                ObjectData od = (ObjectData) state;

                NakedObjectField[] fields = object.getSpecification().getFields();

                for (int i = 0; i < fields.length; i++) {
                    NakedObjectField field = fields[i];
                    Object fieldData = od.getEntry(field.getName());
                    if (!field.isDerived()) {
                        if (field instanceof OneToManyAssociation) {
                            updateOneToManyAssociation(object, loadedObjects, (OneToManyAssociation) field,
                                    (InternalCollectionData) fieldData, context);
                        } else if (field instanceof OneToOneAssociation) {
                            updateOneToOneAssociation(object, loadedObjects, (OneToOneAssociation) field, (Data) fieldData, context);
                        }
                    }
                }
            }
            LOG.debug("object updated " + object.getOid());
        }

    }

    private void updateOneToManyAssociation(NakedObject object, LoadedObjects loadedObjects, OneToManyAssociation field,
            InternalCollectionData collectionData, NakedObjectContext context) {
        InternalCollection collection = (InternalCollection) object.getField(field);
 //       collection.setContext(context);
        if (collection.getOid() == null) {
            collection.setOid(collectionData.getOid());
        }

        Vector original = new Vector();
        int size = collection.size();
        for (int i = 0; i < size; i++) {
            original.addElement(collection.elementAt(i));
        }

        for (int j = 0; j < collectionData.elements.length; j++) {
            NakedObject element = recreateObject2(loadedObjects, (Data) collectionData.elements[j], context);
            if (!collection.contains(element)) {
                LOG.debug("  association " + field + " changed, added " + element.getOid());
                object.setAssociation(field, element);
            } else {
                object.clearAssociation(field, element);
//                original.removeElement(element);
            }
        }

        size = original.size();
        for (int i = 0; i < size; i++) {
            NakedObject element = (NakedObject) original.elementAt(i);
            LOG.debug("  association " + field + " changed, removed " + element.getOid());
            object.clearAssociation(field, element);
        }
    }

    private void updateOneToOneAssociation(NakedObject object, LoadedObjects loadedObjects, OneToOneAssociation field,
            Data fieldData, NakedObjectContext context) {
        if (fieldData == null) {
            object.setValue(field, null);
        } else {
            NakedObject ref = recreateObject2(loadedObjects, fieldData, context);
            if (object.getField(field) != ref) {
                LOG.debug("  association " + field + " changed to " + ref.getOid());
                object.setValue(field, ref);
            }
        }
    }

    public void writeData(TransferableWriter data) {
        data.writeObject(state);
    }

    public void restore(TransferableReader data) {
        state = (Data) data.readObject();
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