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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

class WriteObjectCommand implements PersistenceCommand {
    private final KeyCreator keyCreator;
    private final ObjectAdapter object;
    private final VersionCreator versionCreator;
    private final boolean isUpdate;

    WriteObjectCommand(final boolean isUpdate, final KeyCreator keyCreator, final VersionCreator versionCreator,
        final ObjectAdapter object) {
        this.isUpdate = isUpdate;
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.object = object;
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        final String specName = object.getSpecification().getFullIdentifier();
        final StateWriter writer = ((NoSqlCommandContext) context).createStateWriter(specName);
        final String key = keyCreator.key(object.getOid());
        writer.writeId(key);
        writeFields(writer, specName, object);
        final String user = IsisContext.getAuthenticationSession().getUserName();

        final Version currentVersion = object.getVersion();
        final Version newVersion =
            isUpdate ? versionCreator.nextVersion(currentVersion) : versionCreator.newVersion(user);
        object.setOptimisticLock(newVersion);
        if (newVersion != null) {
            final String version = currentVersion == null ? null : versionCreator.versionString(currentVersion);
            writer.writeVersion(version, versionCreator.versionString(newVersion));
            writer.writeUser(newVersion.getUser());
            writer.writeTime(versionCreator.timeString(newVersion));
        }

        if (isUpdate) {
            ((NoSqlCommandContext) context).update(writer);
        } else {
            ((NoSqlCommandContext) context).insert(writer);
        }

    }

    private void writeFields(final StateWriter writer, final String specName, final ObjectAdapter object) {
        final List<ObjectAssociation> associations = object.getSpecification().getAssociations();
        writer.writeType(specName);
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            final ObjectAdapter field = association.get(object);
            if (association.isOneToManyAssociation()) {
                writeCollection(writer, association, field, keyCreator);
            } else if (association.getSpecification().isValue()) {
                writeValue(writer, association, field);
            } else if (association.getSpecification().isAggregated()) {
                writeAggregatedObject(writer, association, field, keyCreator);
            } else {
                writeReference(writer, association, field, keyCreator);
            }
        }
    }

    private void writeAggregatedObject(final StateWriter writer, final ObjectAssociation association,
        final ObjectAdapter field, final KeyCreator keyCreator) {
        if (field == null) {
            writer.writeField(association.getId(), null);
        } else if (field.getOid() instanceof AggregatedOid) {
            final String specName = field.getSpecification().getFullIdentifier();
            final StateWriter aggregateWriter = writer.addAggregate(association.getId());
            aggregateWriter.writeId(((AggregatedOid) field.getOid()).getId());
            writeFields(aggregateWriter, specName, field);
        } else {
            throw new NoSqlStoreException("Object type is inconsistent with it OID - it should have an AggregatedOid: "
                + field);
        }
    }

    private void writeReference(final StateWriter writer, final ObjectAssociation association,
        final ObjectAdapter reference, final KeyCreator keyCreator) {
        if (reference == null) {
            writer.writeField(association.getId(), null);
        } else {
            final String key = keyCreator.reference(reference);
            writer.writeField(association.getId(), key);
        }
    }

    private void writeValue(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter value) {
        String data;
        if (value == null) {
            data = null;
        } else {
            final EncodableFacet encodeableFacet = value.getSpecification().getFacet(EncodableFacet.class);
            data = encodeableFacet.toEncodedString(value);
        }
        writer.writeField(association.getId(), data);
    }

    private void writeCollection(final StateWriter writer, final ObjectAssociation association,
        final ObjectAdapter collection, final KeyCreator keyCreator) {
        final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        if (association.getSpecification().isAggregated()) {
            final List<StateWriter> elements = new ArrayList<StateWriter>();
            for (final ObjectAdapter element : collectionFacet.iterable(collection)) {
                final StateWriter elementWriter = writer.createElementWriter();
                elementWriter.writeId(((AggregatedOid) element.getOid()).getId());
                writeFields(elementWriter, element.getSpecification().getFullIdentifier(), element);
                elements.add(elementWriter);
            }
            writer.writeCollection(association.getId(), elements);
        } else {
            String refs = "";
            for (final ObjectAdapter element : collectionFacet.iterable(collection)) {
                if (element.isAggregated()) {
                    throw new DomainModelException(
                        "Can't store an aggregated object within a collection that is not exoected aggregates: "
                            + element + " (" + collection + ")");
                }
                refs += keyCreator.reference(element) + "|";
            }
            if (refs.length() > 0) {
                writer.writeField(association.getId(), refs);
            }
        }
    }

    @Override
    public ObjectAdapter onObject() {
        return object;
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("spec", object.getSpecification().getFullIdentifier());
        toString.append("oid", object.getOid());
        return toString.toString();
    }
}
