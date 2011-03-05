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

package org.apache.isis.alternatives.remoting.protocol.internal;

import java.util.Enumeration;

import org.apache.isis.alternatives.remoting.common.data.Data;
import org.apache.isis.alternatives.remoting.common.data.DataFactory;
import org.apache.isis.alternatives.remoting.common.data.common.CollectionData;
import org.apache.isis.alternatives.remoting.common.data.common.EncodableObjectData;
import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.data.common.ReferenceData;
import org.apache.isis.alternatives.remoting.common.exchange.KnownObjectsRequest;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistorUtil;

/**
 * Utility class to create Data objects representing a graph of Isis.
 * 
 * As each object is serialised its resolved state is changed to SERIALIZING; any object that is marked as SERIALIZING
 * is skipped.
 */
public final class ObjectSerializer {

    private final DataFactory dataFactory;
    private final FieldOrderCache fieldOrderCache;

    public ObjectSerializer(final DataFactory dataFactory, final FieldOrderCache fieldOrderCache) {
        this.fieldOrderCache = fieldOrderCache;
        this.dataFactory = dataFactory;
    }

    public final ReferenceData serializeAdapter(final ObjectAdapter adapter, final int depth,
        final KnownObjectsRequest knownObjects) {
        Assert.assertNotNull(adapter);
        return (ReferenceData) serializeObject2(adapter, depth, knownObjects);
    }

    public final EncodableObjectData serializeEncodeable(final ObjectAdapter adapter) {
        final EncodableFacet facet = adapter.getSpecification().getFacet(EncodableFacet.class);
        return this.dataFactory.createValueData(adapter.getSpecification().getFullIdentifier(),
            facet.toEncodedString(adapter));
    }

    private final Data serializeObject2(final ObjectAdapter adapter, final int graphDepth,
        final KnownObjectsRequest knownObjects) {
        Assert.assertNotNull(adapter);

        final ResolveState resolveState = adapter.getResolveState();
        boolean isTransient = adapter.isTransient();

        if (!isTransient && (resolveState.isSerializing() || resolveState.isGhost() || graphDepth <= 0)) {
            Assert.assertNotNull("OID needed for reference", adapter, adapter.getOid());
            return this.dataFactory.createIdentityData(adapter.getSpecification().getFullIdentifier(), adapter.getOid(),
                adapter.getVersion());
        }
        if (isTransient && knownObjects.containsKey(adapter)) {
            return knownObjects.get(adapter);
        }

        boolean withCompleteData = resolveState == ResolveState.TRANSIENT || resolveState == ResolveState.RESOLVED;

        final String type = adapter.getSpecification().getFullIdentifier();
        final Oid oid = adapter.getOid();
        final ObjectData data = this.dataFactory.createObjectData(type, oid, withCompleteData, adapter.getVersion());
        if (isTransient) {
            knownObjects.put(adapter, data);
        }

        final ObjectAssociation[] fields = fieldOrderCache.getFields(adapter.getSpecification());
        final Data[] fieldContent = new Data[fields.length];
        PersistorUtil.start(adapter, adapter.getResolveState().serializeFrom());
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isNotPersisted()) {
                continue;
            }
            final ObjectAdapter field = fields[i].get(adapter);

            if (fields[i].getSpecification().isEncodeable()) {
                if (field == null) {
                    fieldContent[i] = this.dataFactory.createNullData(fields[i].getSpecification().getFullIdentifier());
                } else {
                    fieldContent[i] = serializeEncodeable(field);
                }

            } else if (fields[i].isOneToManyAssociation()) {
                fieldContent[i] = serializeCollection(field, graphDepth - 1, knownObjects);

            } else if (fields[i].isOneToOneAssociation()) {
                if (field == null) {
                    fieldContent[i] =
                        !withCompleteData ? null : this.dataFactory.createNullData(fields[i].getSpecification()
                            .getFullIdentifier());
                } else {
                    fieldContent[i] = serializeObject2(field, graphDepth - 1, knownObjects);
                }

            } else {
                throw new UnknownTypeException(fields[i]);
            }
        }
        PersistorUtil.end(adapter);
        data.setFieldContent(fieldContent);
        return data;
    }

    public CollectionData serializeCollection(final ObjectAdapter collectionAdapter, final int graphDepth,
        final KnownObjectsRequest knownObjects) {
        final Oid oid = collectionAdapter.getOid();
        final String collectionType = collectionAdapter.getSpecification().getFullIdentifier();
        final TypeOfFacet typeOfFacet = collectionAdapter.getSpecification().getFacet(TypeOfFacet.class);
        if (typeOfFacet == null) {
            throw new IsisException("No type of facet for collection " + collectionAdapter);
        }
        final String elementType = typeOfFacet.value().getName();
        final boolean hasAllElements =
            collectionAdapter.isTransient() || collectionAdapter.getResolveState().isResolved();
        ReferenceData[] elements;

        if (hasAllElements) {
            final CollectionFacet collectionFacet = CollectionFacetUtils.getCollectionFacetFromSpec(collectionAdapter);
            final Enumeration e = collectionFacet.elements(collectionAdapter);
            elements = new ReferenceData[collectionFacet.size(collectionAdapter)];
            int i = 0;
            while (e.hasMoreElements()) {
                final ObjectAdapter element = (ObjectAdapter) e.nextElement();
                elements[i++] = serializeAdapter(element, graphDepth, knownObjects);
            }
        } else {
            elements = new ObjectData[0];
        }

        return this.dataFactory.createCollectionData(collectionType, elementType, oid, elements, hasAllElements,
            collectionAdapter.getVersion());
    }

}
