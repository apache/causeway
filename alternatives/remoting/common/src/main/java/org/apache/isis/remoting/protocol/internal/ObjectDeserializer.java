/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.remoting.protocol.internal;

import java.util.Enumeration;

import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.PersistenceSessionHydrator;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.core.runtime.transaction.updatenotifier.UpdateNotifier;
import org.apache.isis.remoting.IsisRemoteException;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.common.CollectionData;
import org.apache.isis.remoting.data.common.EncodableObjectData;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.NullData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.exchange.KnownObjectsRequest;
import org.apache.log4j.Logger;

public class ObjectDeserializer {
    
    private static final Logger LOG = Logger.getLogger(ObjectDeserializer.class);
    
	private final FieldOrderCache fieldOrderCache;
    
    public ObjectDeserializer(FieldOrderCache fieldOrderCache) {
    	this.fieldOrderCache = fieldOrderCache;
	}

    /////////////////////////////////////////////////////////
    // restore
    /////////////////////////////////////////////////////////

    public ObjectAdapter deserialize(final Data data) {
        if (data instanceof CollectionData) {
            return deserializeCollection((CollectionData) data, new KnownObjectsRequest());
        } else {
            return deserializeObject(data, new KnownObjectsRequest());
        }
    }

    public ObjectAdapter deserialize(final Data data, final KnownObjectsRequest knownObjects) {
        if (data instanceof CollectionData) {
            return deserializeCollection((CollectionData) data, knownObjects);
        } else {
            return deserializeObject(data, knownObjects);
        }
    }

    /////////////////////////////////////////////////////////
    // Helper: restoreCollection
    /////////////////////////////////////////////////////////

    private ObjectAdapter deserializeCollection(final CollectionData data, final KnownObjectsRequest knownObjects) {
        final String collectionType = data.getType();
        final ObjectSpecification collectionSpecification = getSpecificationLoader().loadSpecification(
                collectionType);

        /*
         * if we are to deal with internal collections then we need to be able to get the collection from it's
         * parent via its field
         */
        ObjectAdapter collection = getPersistenceSession().createInstance(collectionSpecification);
        if (data.getElements() == null) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("restoring empty collection");
        	}
            return collection;
        } else {
            final ReferenceData[] elements = data.getElements();
            if (LOG.isDebugEnabled()) {
            	LOG.debug("restoring collection " + elements.length + " elements");
            }
            final ObjectAdapter[] initData = new ObjectAdapter[elements.length];
            for (int i = 0; i < elements.length; i++) {
                final ObjectAdapter element = deserializeObject(elements[i], knownObjects);
                if (LOG.isDebugEnabled()) {
                	LOG.debug("restoring collection element :" + element);
                }
                initData[i] = element;
            }
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            facet.init(collection, initData);
            return collection;
        }
    }

    /////////////////////////////////////////////////////////
    // Helper: deserializeObject
    /////////////////////////////////////////////////////////

    private ObjectAdapter deserializeObject(final Data data, final KnownObjectsRequest knownObjects) {
        if (data instanceof NullData) {
            return null;
        } else if (data instanceof ObjectData) {
            ObjectData objectData = (ObjectData) data;
			return deserializeObjectFromObjectData(objectData, knownObjects);
        } else if (data instanceof IdentityData) {
            IdentityData identityData = (IdentityData) data;
			return deserializeObjectFromIdentityData(identityData, knownObjects);
        } else if (data instanceof EncodableObjectData) {
            EncodableObjectData encodableObjectData = (EncodableObjectData) data;
			return deserializeObjectFromEncodableObjectData(encodableObjectData);
        } else {
            throw new UnknownTypeException(data);
        }
    }

    private ObjectAdapter deserializeObjectFromIdentityData(final IdentityData data, final KnownObjectsRequest knownObjects) {
        final Oid oid = data.getOid();
        /*
         * either create a new transient object, get an existing object and update it if data is for resolved
         * object, or create new object and set it
         */
        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter == null) {
            final ObjectSpecification specification = getSpecificationLoader().loadSpecification(data.getType());
            adapter = getHydrator().recreateAdapter(oid, specification);
        }
        return adapter;
    }

    private ObjectAdapter deserializeObjectFromObjectData(final ObjectData data, final KnownObjectsRequest knownObjects) {
        if (knownObjects.containsKey(data)) {
            return knownObjects.get(data);
        }

        final Oid oid = data.getOid();
        /*
         * either create a new transient object, get an existing object and update it if data is for resolved
         * object, or create new object and set it
         */
        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            updateLoadedObject(adapter, data, knownObjects);
        } else if (oid.isTransient()) {
            adapter = deserializeTransient(data, knownObjects);
        } else {
            adapter = deserializePersistentObject(data, oid, knownObjects);
        }

        return adapter;
    }


    private ObjectAdapter deserializeTransient(final ObjectData adapterData, final KnownObjectsRequest knownObjects) {
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(adapterData.getType());

        ObjectAdapter adapter = getHydrator().recreateAdapter(adapterData.getOid(), specification);
        if (LOG.isDebugEnabled()) {
            LOG.debug("restore transient object " + adapter);
        }
        knownObjects.put(adapter, adapterData);
        setUpFields(adapter, adapterData, knownObjects);
        return adapter;
    }


    private ObjectAdapter deserializePersistentObject(final ObjectData data, final Oid oid, final KnownObjectsRequest knownObjects) {
        // unknown object; create an instance
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(data.getType());

        ObjectAdapter adapter = getHydrator().recreateAdapter(oid, specification);
        if (data.getFieldContent() != null) {
            adapter.setOptimisticLock(data.getVersion());
            ResolveState state;
            state = data.hasCompleteData() ? ResolveState.RESOLVING : ResolveState.RESOLVING_PART;
            if (LOG.isDebugEnabled()) {
            	LOG.debug("restoring existing object (" + state.name() + ") " + adapter);
            }
            setupFields(adapter, data, state, knownObjects);
        }
        return adapter;
    }


    private ObjectAdapter deserializeObjectFromEncodableObjectData(final EncodableObjectData encodeableObjectData) {
        if (encodeableObjectData.getEncodedObjectData() == null) {
            return null;
        } 
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(
		        encodeableObjectData.getType());
		final EncodableFacet encoder = spec.getFacet(EncodableFacet.class);
		ObjectAdapter adapter = encoder.fromEncodedString(encodeableObjectData.getEncodedObjectData());
		return adapter;
    }


    /////////////////////////////////////////////////////////
    // Helpers: updateLoadedObject
    /////////////////////////////////////////////////////////

    private void updateLoadedObject(
    		final ObjectAdapter adapter, 
    		final ObjectData adapterData, 
    		final KnownObjectsRequest knownObjects) {
        // object known and we have all the latest data; update/resolve the object
        if (adapterData.getFieldContent() != null) {
            adapter.setOptimisticLock(adapterData.getVersion());
            final ResolveState state = nextState(adapter.getResolveState(), adapterData.hasCompleteData());
            if (state != null) {
                LOG.debug("updating existing object (" + state.name() + ") " + adapter);
                setupFields(adapter, adapterData, state, knownObjects);
                getUpdateNotifier().addChangedObject(adapter);
            }
        } else {
            if (adapterData.getVersion() != null && adapterData.getVersion().different(adapter.getVersion())) {
                // TODO reload the object
            }
        }
    }



    /////////////////////////////////////////////////////////
    // Helpers: setupFields
    /////////////////////////////////////////////////////////

    private void setUpCollectionField(
            final ObjectAdapter adapter,
            final ObjectData parentData,
            final ObjectAssociation field,
            final CollectionData collectionContentData,
            final KnownObjectsRequest knownObjects) {
        if (collectionContentData.hasAllElements()) {
            setUpCollectionFieldForEntireContents(adapter, field,
					collectionContentData, knownObjects);
        } else {
            setUpCollectionFieldForNoContents(parentData, adapter, field);
        }
    }

	private void setUpCollectionFieldForNoContents(
			final ObjectData parentData, final ObjectAdapter adapter,
			final ObjectAssociation field) {
		final ObjectAdapter collection = field.get(adapter);
		if (collection.getResolveState() == ResolveState.GHOST) {
			return;
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("No data for collection: " + field.getId());
		}
		
		Version adapterVersion = adapter.getVersion();
		Version parentVersion = parentData.getVersion();
		if (adapterVersion.different(parentVersion)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("clearing collection as versions differ: " + adapter.getVersion() + " " + parentData.getVersion());
			}
		    final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
		    facet.init(collection, new ObjectAdapter[0]);
		    collection.changeState(ResolveState.GHOST);
		}
	}

	private void setUpCollectionFieldForEntireContents(
			final ObjectAdapter adapter, 
			final ObjectAssociation field,
			final CollectionData collectionContentData,
			final KnownObjectsRequest knownObjects) {
		final int size = collectionContentData.getElements().length;
		final ObjectAdapter[] elements = new ObjectAdapter[size];
		for (int j = 0; j < elements.length; j++) {
		    elements[j] = deserializeObject(collectionContentData.getElements()[j], knownObjects);
		    if (LOG.isDebugEnabled()) {
		    	LOG.debug("adding element to " + field.getId() + ": " + elements[j]);
		    }
		}

		final ObjectAdapter col = field.get(adapter);
		final ResolveState initialState = col.getResolveState();
		final ResolveState state = nextState(initialState, collectionContentData.hasAllElements());
		if (state != null) {
		    PersistorUtil.start(col, state);
		    final ObjectAdapter collection = ((OneToManyAssociation) field).get(adapter);
		    final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
		    facet.init(collection, elements);
		    PersistorUtil.end(col);
		} else {
		    LOG.warn("not initialising collection " + col + " due to current state " + initialState);
		}
	}

    private void setupFields(
            final ObjectAdapter adapter,
            final ObjectData adapterData,
            final ResolveState state,
            final KnownObjectsRequest knownObjects) {
        if (adapter.getResolveState().isDeserializable(state)) {
            PersistorUtil.start(adapter, state);
            setUpFields(adapter, adapterData, knownObjects);
            PersistorUtil.end(adapter);
        }
    }

    private void setUpFields(
    		final ObjectAdapter adapter,
    		final ObjectData adapterData, 
    		final KnownObjectsRequest knownObjects) {
        final Data[] fieldContent = adapterData.getFieldContent();
        if (fieldContent != null && fieldContent.length > 0) {
            final ObjectAssociation[] fields = fieldOrderCache.getFields(adapter.getSpecification());
            if (fields.length != fieldContent.length) {
                throw new IsisRemoteException("Data received for different number of fields; expected " + fields.length
                        + ", but was " + fieldContent.length);
            }
            for (int i = 0; i < fields.length; i++) {
                final ObjectAssociation field = fields[i];
                final Data fieldData = fieldContent[i];
                if (fieldData == null || field.isNotPersisted()) {
                    LOG.debug("no data for field " + field.getId());
                    continue;
                }

                if (field.isOneToManyAssociation()) {
                    setUpCollectionField(adapter, adapterData, field, (CollectionData) fieldData, knownObjects);
                } else if (field.getSpecification().isEncodeable()) {
                    setUpEncodedField(adapter, (OneToOneAssociation) field, fieldData);
                } else {
                    setUpReferenceField(adapter, (OneToOneAssociation) field, fieldData, knownObjects);
                }
            }
        }
    }

    private void setUpReferenceField(
            final ObjectAdapter adapter,
            final OneToOneAssociation field,
            final Data data,
            final KnownObjectsRequest knownObjects) {
        ObjectAdapter associate;
        associate = deserializeObject(data, knownObjects);
        if (LOG.isDebugEnabled()) {
        	LOG.debug("setting association for field " + field.getId() + ": " + associate);
        }
        field.initAssociation(adapter, associate);
    }

    private void setUpEncodedField(
    		final ObjectAdapter adapter, final OneToOneAssociation field, final Data data) {
        String value;
        if (data instanceof NullData) {
            field.initAssociation(adapter, null);
        } else {
            value = ((EncodableObjectData) data).getEncodedObjectData();
            final EncodableFacet encoder = field.getSpecification().getFacet(EncodableFacet.class);
            final ObjectAdapter valueAdapter = encoder.fromEncodedString(value);
            if (LOG.isDebugEnabled()) {
            	LOG.debug("setting value for field " + field.getId() + ": " + valueAdapter);
            }
            field.initAssociation(adapter, valueAdapter);
        }
    }


    /////////////////////////////////////////////////////////
    // madePersistent
    /////////////////////////////////////////////////////////

    /**
     * Equivalent to {@link PersistenceSessionObjectStore#remapAsPersistent(ObjectAdapter)}.
     */
    public void madePersistent(
    		final ObjectAdapter adapter, final ObjectData adapterData) {
        if (adapterData == null) {
            return;
        }

        if (adapter.isTransient() && 
        	adapter.getSpecification().persistability() != Persistability.TRANSIENT) {

        	getAdapterManager().getAdapterFor(adapterData.getOid()); // causes OID to be updated
            adapter.setOptimisticLock(adapterData.getVersion());
            adapter.changeState(ResolveState.RESOLVED);
        }

        final Data[] fieldData = adapterData.getFieldContent();
        if (fieldData == null) {
            return;
        }
        final ObjectAssociation[] fields = fieldOrderCache.getFields(adapter.getSpecification());
        for (int i = 0; i < fieldData.length; i++) {
            if (fieldData[i] == null) {
                continue;
            }
            if (fields[i].isOneToOneAssociation()) {
                final ObjectAdapter field = ((OneToOneAssociation) fields[i]).get(adapter);
                final ObjectData fieldContent = (ObjectData) adapterData.getFieldContent()[i];
                if (field != null) {
                    madePersistent(field, fieldContent);
                }
            } else if (fields[i].isOneToManyAssociation()) {
                final CollectionData collectionData = (CollectionData) adapterData.getFieldContent()[i];
                final ObjectAdapter collectionAdapter = fields[i].get(adapter);
                if (!collectionAdapter.isPersistent()) {
                    collectionAdapter.changeState(ResolveState.RESOLVED);
                }
                final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collectionAdapter);
                final Enumeration elements = facet.elements(collectionAdapter);
                for (int j = 0; j < collectionData.getElements().length; j++) {
                    final ObjectAdapter element = (ObjectAdapter) elements.nextElement();
                    if (collectionData.getElements()[j] instanceof ObjectData) {
                        final ObjectData elementData = (ObjectData) collectionData.getElements()[j];
                        madePersistent(element, elementData);
                    }
                }
            }
        }
    }


    /////////////////////////////////////////////////////////
    // Helpers: nextState
    /////////////////////////////////////////////////////////

    private ResolveState nextState(final ResolveState initialState, final boolean complete) {
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


    /////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    /////////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    private UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private PersistenceSessionHydrator getHydrator() {
        return getPersistenceSession();
    }

}
