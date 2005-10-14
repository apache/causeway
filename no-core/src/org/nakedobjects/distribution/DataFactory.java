package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Assert;

import java.util.Enumeration;


/**
 * Utility class to create Data objects representing a graph of NakedObjects.
 * 
 * As each object is serialised its resovled state is changed to SERIALIZING; any object that is marked as
 * SERIALIZING is skipped.
 */
public abstract class DataFactory {
    private int persistentGraphDepth = 100;

    private CollectionData createCollectionData(NakedCollection collection, boolean recursePersistentObjects, int depth) {
        Oid oid = collection.getOid();
        String type = collection.getSpecification().getFullName();
        boolean hasAllElements = collection.getResolveState() == ResolveState.TRANSIENT
                || collection.getResolveState() == ResolveState.RESOLVED;
        ObjectData[] elements;

        if (hasAllElements) {
            Enumeration e = collection.elements();
            elements = new ObjectData[collection.size()];
            int i = 0;
            while (e.hasMoreElements()) {
                NakedObject element = (NakedObject) e.nextElement();
                elements[i++] = createObjectData(element, recursePersistentObjects, depth);
            }
        } else {
            elements = new ObjectData[0];
        }

        return createCollectionData(oid, type, elements, hasAllElements, !collection.getResolveState().isGhost(), collection
                .getVersion());
    }

    /**
     * Creates an ObjectData that contains all the data for all the objects in the graph. This allows the
     * client to recieve all data it might need without having to return to the server to get referenced
     * objects.
     */
    public final ObjectData createCompletePersistentGraph(NakedObject object) {
        return createObjectData(object, true, persistentGraphDepth);
    }

    public final Data createDataForParameter(String type, Naked object) {
        if (object == null) {
            return createNullData(type);
        }

        if (object.getSpecification().isObject()) {
            NakedObject nakedObject = (NakedObject) object;
            return createObjectData(nakedObject, false, persistentGraphDepth);
        } else if (object.getSpecification().isValue()) {
            return createValueData(object);
        } else {
            throw new IllegalArgumentException("Expected a naked object or a naked value, but got " + object);
        }
    }

    protected abstract NullData createNullData(String type);

    /**
     * Creates an ObjectData that contains the data for the specified object, but not the data for any
     * referenced objects. For each referenced object only the reference is passed across.
     */
    public final ObjectData createForUpdate(NakedObject object) {
        return createObjectData(object, true, 1);
    }

    /**
     * Creates an ObjectData that contains all the data for all the transient objects in the specified
     * transient object. For any referenced persistent object in the graph, only the reference is passed
     * across.
     */
    public final ObjectData createMakePersistentGraph(NakedObject object) {
        Assert.assertTrue(object.getResolveState().isTransient());
        return createObjectData(object, false, persistentGraphDepth);
    }

    protected final ObjectData createObjectData(NakedObject object, boolean recursePersistentObjects, int depth) {
        if (object == null) {
            return null;
        }

        boolean nextLevel = object.getResolveState().isTransient() || recursePersistentObjects;

        Oid oid = object.getOid();
        NakedObjectSpecification specification = object.getSpecification();
        String type = specification.getFullName();
        ResolveState resolveState = object.getResolveState();
        boolean isComplete = object.getResolveState() == ResolveState.TRANSIENT
                || object.getResolveState() == ResolveState.RESOLVED;

        Object[] fieldContent;
        if (resolveState.isSerializing() || !nextLevel || depth == 0 || resolveState.isGhost()) {
            fieldContent = null;
        } else {
            NakedObjectField[] fields = specification.getFields();
            fieldContent = new Object[fields.length];

            NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
            for (int i = 0; i < fields.length; i++) {
                Naked field = object.getField(fields[i]);
                if (field == null && isComplete) {
                    fieldContent[i] = createNullData(fields[i].getSpecification().getFullName());
                } else if (field == null && !isComplete) {
                    fieldContent[i] = null;
                } else if (fields[i].isValue()) {
                    fieldContent[i] = createValueData(field);
                } else if (fields[i].isCollection()) {
                    fieldContent[i] = createCollectionData((NakedCollection) field, recursePersistentObjects, depth - 1);
                } else {
                    fieldContent[i] = createObjectData((NakedObject) field, recursePersistentObjects, depth - 1);
                }
            }
            NakedObjects.getObjectLoader().end(object);
        }

        return createObjectData(oid, type, fieldContent, isComplete, !resolveState.isGhost(), object.getVersion());
    }

    protected abstract ObjectData createObjectData(
            Oid oid,
            String type,
            Object[] fieldContent,
            boolean hasCompleteData,
            boolean hasVersion,
            long version);

    protected abstract CollectionData createCollectionData(
            Oid oid,
            String type,
            ObjectData[] elements,
            boolean hasAllElements,
            boolean hasVersion,
            long version);

    /**
     * Creates a ReferenceData that contains the type, version and OID for the specified object. This can only
     * be used for peristent objects.
     */
    public final ReferenceData createReference(NakedObject object) {
        Assert.assertNotNull(object.getOid());
        return createReferenceData(object.getSpecification().getFullName(), object.getOid(), !object.getResolveState().isGhost(),
                object.getVersion());
    }

    protected abstract ReferenceData createReferenceData(String type, Oid oid, boolean hasVersion, long version);

    private final ValueData createValueData(Naked object) {
        return createValueData(object.getSpecification().getFullName(), ((NakedValue) object).getObject());
    }

    protected abstract ValueData createValueData(String fullName, Object object);

    public int getPersistentGraphDepth() {
        return persistentGraphDepth;
    }

    // TODO add property for update depth
    public void setPersistentGraphDepth(int persistentGraphDepth) {
        this.persistentGraphDepth = persistentGraphDepth;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_PersistentGraphDepth(int persistentGraphDepth) {
        this.persistentGraphDepth = persistentGraphDepth;
    }

    public final ObjectData createDataForActionTarget(NakedObject object) {
        return createObjectData(object, false, 100);
    }

    public Data createActionResult(Naked result) {
        if (result == null) {
            return createNullData("");
        } else if (result instanceof NakedCollection) {
            return createCollectionData((NakedCollection) result, true, persistentGraphDepth);
        } else if (result instanceof NakedObject) {
            return createCompletePersistentGraph((NakedObject) result);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    // TODO pass accross only the field within the object
    public Data createForResolveField(NakedObject object, String fieldName) {
        Oid oid = object.getOid();
        NakedObjectSpecification specification = object.getSpecification();
        String type = specification.getFullName();
        ResolveState resolveState = object.getResolveState();

        Object[] fieldContent;
        NakedObjectField[] fields = specification.getFields();
        fieldContent = new Object[fields.length];

        NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                Naked field = object.getField(fields[i]);
                if (field == null) {
                    fieldContent[i] = createNullData(fields[i].getSpecification().getFullName());
                } else if (fields[i].isValue()) {
                    fieldContent[i] = createValueData(field);
                } else if (fields[i].isCollection()) {
                    fieldContent[i] = createCollectionData((NakedCollection) field, true, persistentGraphDepth);
                } else {
                    fieldContent[i] = createObjectData((NakedObject) field, true, persistentGraphDepth);
                }
                break;
            }
        }
        NakedObjects.getObjectLoader().end(object);

        // TODO remove the fudge - needed as collections are part of parents, hence parent object gets set as 
        // resolving (is not a ghost) yet it has no version number
        //return createObjectData(oid, type, fieldContent, resolveState.isResolved(), !resolveState.isGhost(), object.getVersion());
        return createObjectData(oid, type, fieldContent, resolveState.isResolved(), false, object.getVersion());
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
