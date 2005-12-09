package org.nakedobjects.distribution;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Version;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Utility class to create Data objects representing a graph of NakedObjects.
 * 
 * As each object is serialised its resovled state is changed to SERIALIZING; any object that is marked as
 * SERIALIZING is skipped.
 */
public final class ObjectEncoder {
    private int actionGraphDepth = 100;
    private DataStructure dataStructure = new DataStructure();
    private int persistentGraphDepth = 100;
    private int updateGraphDepth = 1;
    private DataFactory factory;

    public ServerActionResultData createActionResult(
            Naked returns,
            ObjectData[] updatesData,
            ObjectData persistedTarget,
            ObjectData[] persistedParameters,
            String[] messages,
            String[] warnings) {
        Data result;
        if (returns == null) {
            result = factory.createNullData("");
        } else if (returns instanceof NakedCollection) {
            result = createCollectionData((NakedCollection) returns, true, persistentGraphDepth, new Hashtable());
        } else if (returns instanceof NakedObject) {
            result = createCompletePersistentGraph((NakedObject) returns);
        } else {
            throw new NakedObjectRuntimeException();
        }

        return factory.createActionResultData(result, updatesData, persistedTarget, persistedParameters, messages, warnings);
    }

    private CollectionData createCollectionData(
            NakedCollection collection,
            boolean recursePersistentObjects,
            int depth,
            Hashtable previous) {
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
                elements[i++] = createObjectData(element, recursePersistentObjects, depth, previous);
            }
        } else {
            elements = new ObjectData[0];
        }

        return factory.createCollectionData(oid, type, elements, hasAllElements, collection.getVersion());
    }

    /**
     * Creates an ObjectData that contains all the data for all the objects in the graph. This allows the
     * client to recieve all data it might need without having to return to the server to get referenced
     * objects.
     */
    public final ObjectData createCompletePersistentGraph(NakedObject object) {
        return createObjectData(object, true, persistentGraphDepth, new Hashtable());
    }

    public final ObjectData createDataForActionTarget(NakedObject object) {
        return createObjectData(object, false, actionGraphDepth, new Hashtable());
    }

    public ObjectData createDataForChangedObject(NakedObject object) {
        return createObjectData(object, true, 1, new Hashtable());
    }

    private final Data createDataForParameter(String type, Naked object) {
        if (object == null) {
            return factory.createNullData(type);
        }

        if (object.getSpecification().isObject()) {
            NakedObject nakedObject = (NakedObject) object;
            return createObjectData(nakedObject, false, persistentGraphDepth, new Hashtable());
        } else if (object.getSpecification().isValue()) {
            return createValueData(object);
        } else {
            throw new IllegalArgumentException("Expected a naked object or a naked value, but got " + object);
        }
    }

    public final Data[] createDataForParameters(NakedObjectSpecification[] parameterTypes, Naked[] parameters) {
        Data parameterData[] = new Data[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Naked parameter = parameters[i];
            String type = parameterTypes[i].getFullName();
            parameterData[i] = createDataForParameter(type, parameter);
        }
        return parameterData;
    }

    // TODO pass accross only the field within the object
    public Data createForResolveField(NakedObject object, String fieldName) {
        Oid oid = object.getOid();
        NakedObjectSpecification specification = object.getSpecification();
        String type = specification.getFullName();
        ResolveState resolveState = object.getResolveState();

        Data[] fieldContent;
        NakedObjectField[] fields = getFields(specification);
        fieldContent = new Data[fields.length];

        NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
        Hashtable previous = new Hashtable();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(fieldName)) {
                Naked field = object.getField(fields[i]);
                if (field == null) {
                    fieldContent[i] = factory.createNullData(fields[i].getSpecification().getFullName());
                } else if (fields[i].isValue()) {
                    fieldContent[i] = createValueData(field);
                } else if (fields[i].isCollection()) {
                    fieldContent[i] = createCollectionData((NakedCollection) field, true, persistentGraphDepth, previous);
                } else {
                    fieldContent[i] = createObjectData((NakedObject) field, true, persistentGraphDepth, previous);
                }
                break;
            }
        }
        NakedObjects.getObjectLoader().end(object);

        // TODO remove the fudge - needed as collections are part of parents, hence parent object gets set as
        // resolving (is not a ghost) yet it has no version number
        // return createObjectData(oid, type, fieldContent, resolveState.isResolved(),
        // !resolveState.isGhost(), object.getVersion());
        ObjectData data = factory.createObjectData(oid, type, resolveState.isResolved(), object.getVersion());
        data.setFieldContent(fieldContent);
        return data;
        // return createObjectData(oid, type, fieldContent, resolveState.isResolved(), object.getVersion());
    }

    /**
     * Creates an ObjectData that contains the data for the specified object, but not the data for any
     * referenced objects. For each referenced object only the reference is passed across.
     */
    public final ObjectData createForUpdate(NakedObject object) {
        return createObjectData(object, true, updateGraphDepth, new Hashtable());
    }

    /**
     * Creates a a graph of ReferenceData objects to transfer the OIDs and Versions for each object that was
     * made persistent during the makePersistent call.
     * 
     * @param updateNotifier
     */
    public ObjectData createMadePersistentGraph(ObjectData data, NakedObject object, SingleResponseUpdateNotifier updateNotifier) {
        if (object.getResolveState().isSerializing()) {
            return data;
        }

        if (data == null || data.getOid() != null) {
            return null;
        }

        Oid oid = object.getOid();
        String type = data.getType();
        ReferenceData[] fieldContent = data.getFieldContent() == null ? null : new ReferenceData[data.getFieldContent().length];
        Version version = object.getVersion();

        updateNotifier.removeUpdateFor(object);

        NakedObjectField[] fields = dataStructure.getFields(object.getSpecification());

        NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isValue() || data.getFieldContent()[i] == null) {
                continue;
            } else if (fields[i].isCollection()) {
                CollectionData f = (CollectionData) data.getFieldContent()[i];
                ObjectData[] elements = new ObjectData[f.getElements().length];
                NakedCollection coll = (NakedCollection) object.getField(fields[i]);
                for (int j = 0; j < f.getElements().length; j++) {
                    ObjectData element = f.getElements()[j];
                    if (element != null && element.getOid() == null) {
                        NakedObject el = coll.elementAt(j);
                        elements[j] = createMadePersistentGraph(element, el, updateNotifier);
                    }
                }
                fieldContent[i] = factory.createCollectionData(coll.getOid(), f.getType(), elements, f.hasAllElements(), coll
                        .getVersion());
            } else {
                Data f = data.getFieldContent()[i];
                if (f != null && !(f instanceof NullData) && ((ObjectData) f).getOid() == null) {
                    NakedObject o = (NakedObject) object.getField(fields[i]);
                    fieldContent[i] = createMadePersistentGraph(((ObjectData) f), o, updateNotifier);
                }
            }

        }
        NakedObjects.getObjectLoader().end(object);

        ObjectData createReferenceData = factory.createObjectData(oid, type, true, version);
        createReferenceData.setFieldContent(fieldContent);
        return createReferenceData;
    }

    /**
     * Creates an ObjectData that contains all the data for all the transient objects in the specified
     * transient object. For any referenced persistent object in the graph, only the reference is passed
     * across.
     */
    public final ObjectData createMakePersistentGraph(NakedObject object) {
        Assert.assertTrue("transient", object.getResolveState().isTransient());
        return createObjectData(object, false, persistentGraphDepth, new Hashtable());
    }

    private final ObjectData createObjectData(NakedObject object, boolean recursePersistentObjects, int depth, Hashtable previous) {
        if (object == null) {
            return null;
        }

        boolean nextLevel = object.getResolveState().isTransient() || recursePersistentObjects;

        if (previous.containsKey(object)) {
            return (ObjectData) previous.get(object);
        }

        Oid oid = object.getOid();
        NakedObjectSpecification specification = object.getSpecification();
        String type = specification.getFullName();
        ResolveState resolveState = object.getResolveState();
        boolean isComplete = object.getResolveState() == ResolveState.TRANSIENT
                || object.getResolveState() == ResolveState.RESOLVED;

        ObjectData data = factory.createObjectData(oid, type, isComplete, object.getVersion());
        previous.put(object, data);

        Data[] fieldContent;
        if (resolveState.isSerializing() || !nextLevel || depth == 0 || resolveState.isGhost()) {
            fieldContent = null;
        } else {
            NakedObjectField[] fields = getFields(specification);
            fieldContent = new Data[fields.length];
            NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
            for (int i = 0; i < fields.length; i++) {
                Naked field = object.getField(fields[i]);
                if (fields[i].isDerived()) {
                    fieldContent[i] = null;
                } else if (field == null && isComplete) {
                    fieldContent[i] = factory.createNullData(fields[i].getSpecification().getFullName());
                } else if (field == null && !isComplete) {
                    fieldContent[i] = null;
                } else if (fields[i].isValue()) {
                    fieldContent[i] = createValueData(field);
                } else if (fields[i].isCollection()) {
                    fieldContent[i] = createCollectionData((NakedCollection) field, recursePersistentObjects, depth - 1, previous);
                } else {
                    if (recursePersistentObjects || field.getOid() == null) {
                        fieldContent[i] = createObjectData((NakedObject) field, recursePersistentObjects, depth - 1, previous);
                    } else {
                        fieldContent[i] = createReference((NakedObject) field);
                    }
                }
            }
            NakedObjects.getObjectLoader().end(object);
        }

        data.setFieldContent(fieldContent);
        // return createObjectData(oid, type, fieldContent, isComplete, object.getVersion());
        return data;
    }

    /**
     * Creates a ReferenceData that contains the type, version and OID for the specified object. This can only
     * be used for peristent objects.
     */
    public final ReferenceData createReference(NakedObject object) {
        Assert.assertNotNull(object.getOid());
        return factory.createReferenceData(object.getSpecification().getFullName(), object.getOid(), object.getVersion());
    }
    
    private final ValueData createValueData(Naked object) {
        return factory.createValueData(object.getSpecification().getFullName(), ((NakedValue) object).getObject());
    }

    private NakedObjectField[] getFields(NakedObjectSpecification specification) {
        // return specification.getFields();
        return dataStructure.getFields(specification);
    }

    /**
     * .NET property
     * 
     * @property
     * @see #setActionGraphDepth(int)
     */
    public void set_ActionGraphDepth(int actionGraphDepth) {
        setActionGraphDepth(actionGraphDepth);
    }

    /**
     * .NET property
     * 
     * @property
     * @see #setPersistentGraphDepth(int)
     */
    public void set_PersistentGraphDepth(int persistentGraphDepth) {
        setPersistentGraphDepth(persistentGraphDepth);
    }

    /**
     * .NET property
     * 
     * @property
     * @see #setUpdateGraphDepth(int)
     */
    public void set_UpdateGraphDepth(int updateGraphDepth) {
        setUpdateGraphDepth(updateGraphDepth);
    }

    /**
     * Specifies the maximum depth to recurse when creaing data graphs for the method
     * createDataForActionTarget. Defaults to 100.
     */
    public void setActionGraphDepth(int actionGraphDepth) {
        this.actionGraphDepth = actionGraphDepth;
    }

    /**
     * Specifies the maximum depth to recurse when creaing data graphs of persistent objects. Defaults to 100.
     */
    public void setPersistentGraphDepth(int persistentGraphDepth) {
        this.persistentGraphDepth = persistentGraphDepth;
    }

    /**
     * Specifies the maximum depth to recurse when creaing data graphs for updates. Defaults to 1.
     */
    public void setUpdateGraphDepth(int updateGraphDepth) {
        this.updateGraphDepth = updateGraphDepth;
    }

    public void setDataFactory(DataFactory factory) {
        this.factory = factory;
    }

    public void set_DataFactory(DataFactory factory) {
        setDataFactory(factory);
    }

    public ClientActionResultData createClientActionResultData(ObjectData[] madePersistent, Version[] changedVersion) {
        return factory.createActionResultData(madePersistent, changedVersion);
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
