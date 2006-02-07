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
import org.nakedobjects.utility.UnknownTypeException;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Utility class to create Data objects representing a graph of NakedObjects.
 * 
 * As each object is serialised its resovled state is changed to SERIALIZING; any object that is marked as
 * SERIALIZING is skipped.
 */
public final class ObjectEncoder {
    public static class KnownTransients {
        private Hashtable knownObjects = new Hashtable();

        private KnownTransients() {}

        private boolean containsKey(NakedObject object) {
            return knownObjects.containsKey(object);
        }

        private ObjectData get(NakedObject object) {
            return (ObjectData) knownObjects.get(object);
        }

        private void put(NakedObject object, ObjectData data) {
            knownObjects.put(object, data);
        }
    }

    public static KnownTransients createKnownTransients() {
        return new KnownTransients();
    }

    private int actionGraphDepth = 0;
    private DataStructure dataStructure = new DataStructure();
    private DataFactory factory;
    private int persistentGraphDepth = 100;
    private int updateGraphDepth = 1;

    public final ReferenceData createActionTarget(NakedObject object) {
        return serializeObject(object, actionGraphDepth);
    }

    public ClientActionResultData createClientActionResult(ObjectData[] madePersistent, Version[] changedVersion) {
        return factory.createActionResultData(madePersistent, changedVersion);
    }

    private CollectionData createCollection(NakedCollection collection, int graphDepth, KnownTransients knownObjects) {
        Oid oid = collection.getOid();
        String type = collection.getSpecification().getFullName();
        boolean hasAllElements = collection.getResolveState().isTransient() || collection.getResolveState().isResolved();
        ReferenceData[] elements;

        if (hasAllElements) {
            Enumeration e = collection.elements();
            elements = new ReferenceData[collection.size()];
            int i = 0;
            while (e.hasMoreElements()) {
                NakedObject element = (NakedObject) e.nextElement();
                elements[i++] = serializeObject(element, graphDepth, knownObjects);
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
        return (ObjectData) serializeObject(object, persistentGraphDepth);
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
        KnownTransients knownObjects = new KnownTransients();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(fieldName)) {
                Naked field = object.getField(fields[i]);
                if (field == null) {
                    fieldContent[i] = factory.createNullData(fields[i].getSpecification().getFullName());
                } else if (fields[i].isValue()) {
                    fieldContent[i] = createValueData(field);
                } else if (fields[i].isCollection()) {
                    fieldContent[i] = createCollection((NakedCollection) field, persistentGraphDepth, knownObjects);
                } else {
                    fieldContent[i] = serializeObject((NakedObject) field, persistentGraphDepth, knownObjects);
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
        return (ObjectData) serializeObject(object, updateGraphDepth);
    }

    public ObjectData createGraphForChangedObject(NakedObject object, KnownTransients knownObjects) {
        return (ObjectData) serializeObject(object, 1, knownObjects);
    }

    /**
     * Creates a a graph of ReferenceData objects to transfer the OIDs and Versions for each object that was
     * made persistent during the makePersistent call.
     * 
     * @param updateNotifier
     */
    public ObjectData createMadePersistentGraph(ObjectData data, NakedObject object, SingleResponseUpdateNotifier updateNotifier) {
        if (object.getResolveState().isSerializing()) {
            return null;
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
                    ReferenceData element = f.getElements()[j];
                    if (element instanceof ObjectData) {
                        NakedObject el = coll.elementAt(j);
                        elements[j] = createMadePersistentGraph((ObjectData) element, el, updateNotifier);
                    }
                }
                fieldContent[i] = factory.createCollectionData(coll.getOid(), f.getType(), elements, f.hasAllElements(), coll
                        .getVersion());
            } else if (fields[i].isObject()) {
                Data f = data.getFieldContent()[i];
                if (f != null && !(f instanceof NullData) && ((ReferenceData) f).getOid() == null) {
                    NakedObject o = (NakedObject) object.getField(fields[i]);
                    fieldContent[i] = createMadePersistentGraph(((ObjectData) f), o, updateNotifier);
                }
            } else {
                throw new UnknownTypeException();
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
    public final ObjectData createMakePersistentGraph(NakedObject object, KnownTransients knownObjects) {
        Assert.assertTrue("transient", object.getResolveState().isTransient());
        return (ObjectData) serializeObject(object, 1, knownObjects);
    }

    private final Data createParameter(String type, Naked object) {
        if (object == null) {
            return factory.createNullData(type);
        }

        if (object.getSpecification().isObject()) {
            NakedObject nakedObject = (NakedObject) object;
            return serializeObject(nakedObject, 0, new KnownTransients());
        } else if (object.getSpecification().isValue()) {
            return createValueData(object);
        } else {
            throw new UnknownTypeException(object.getSpecification());
        }
    }

    public final Data[] createParameters(NakedObjectSpecification[] parameterTypes, Naked[] parameters) {
        Data parameterData[] = new Data[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Naked parameter = parameters[i];
            String type = parameterTypes[i].getFullName();
            parameterData[i] = createParameter(type, parameter);
        }
        return parameterData;
    }

    /**
     * Creates a ReferenceData that contains the type, version and OID for the specified object. This can only
     * be used for peristent objects.
     */
    public final IdentityData createIdentityData(NakedObject object) {
        Assert.assertNotNull("OID needed for reference", object, object.getOid());
        return factory.createIdentityData(object.getSpecification().getFullName(), object.getOid(), object.getVersion());
    }

    public ServerActionResultData createServerActionResult(
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
            result = createCollection((NakedCollection) returns, persistentGraphDepth, new KnownTransients());
        } else if (returns instanceof NakedObject) {
            result = createCompletePersistentGraph((NakedObject) returns);
        } else {
            throw new UnknownTypeException(returns);
        }

        return factory.createActionResultData(result, updatesData, persistedTarget, persistedParameters, messages, warnings);
    }

    private final ValueData createValueData(Naked object) {
        return factory.createValueData(object.getSpecification().getFullName(), ((NakedValue) object).getObject());
    }

    private NakedObjectField[] getFields(NakedObjectSpecification specification) {
        return dataStructure.getFields(specification);
    }

    private final ReferenceData serializeObject(NakedObject object, int graphDepth) {
        Assert.assertNotNull(object);

        return (ReferenceData) serializeObject2(object, graphDepth, new KnownTransients());
    }

    private final ReferenceData serializeObject(NakedObject object, int depth, KnownTransients knownTransients) {
        Assert.assertNotNull(object);

        return (ReferenceData) serializeObject2(object, depth, knownTransients);
    }

    private final Data serializeObject2(NakedObject object, int graphDepth, KnownTransients knownTransients) {
        Assert.assertNotNull(object);

        ResolveState resolveState = object.getResolveState();
        boolean isTransient = resolveState.isTransient();

        if (!isTransient && (resolveState.isSerializing() || resolveState.isGhost() || graphDepth <= 0)) {
            return createIdentityData(object);
        }
        if (isTransient && knownTransients.containsKey(object)) {
            return (ObjectData) knownTransients.get(object);
        }

        boolean withCompleteData = resolveState == ResolveState.TRANSIENT || resolveState == ResolveState.RESOLVED;

        String type = object.getSpecification().getFullName();
        Oid oid = object.getOid();
        ObjectData data = factory.createObjectData(oid, type, withCompleteData, object.getVersion());
        if (isTransient) {
            knownTransients.put(object, data);
        }

        NakedObjectField[] fields = getFields(object.getSpecification());
        Data[] fieldContent = new Data[fields.length];
        NakedObjects.getObjectLoader().start(object, object.getResolveState().serializeFrom());
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived()) {
                continue;
            }
            Naked field = object.getField(fields[i]);

            if (fields[i].isValue()) {
                fieldContent[i] = createValueData(field);

            } else if (fields[i].isCollection()) {
                fieldContent[i] = createCollection((NakedCollection) field, graphDepth - 1, knownTransients);

            } else if (fields[i].isObject()) {
                if (field == null) {
                    fieldContent[i] = !withCompleteData ? null : factory.createNullData(fields[i].getSpecification()
                            .getFullName());
                } else {
                    fieldContent[i] = serializeObject2((NakedObject) field, graphDepth - 1, knownTransients);
                }

            } else {
                throw new UnknownTypeException(fields[i]);
            }
        }
        NakedObjects.getObjectLoader().end(object);
        data.setFieldContent(fieldContent);
        return data;
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
     */
    public void set_DataFactory(DataFactory factory) {
        setDataFactory(factory);
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

    public void setDataFactory(DataFactory factory) {
        this.factory = factory;
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
