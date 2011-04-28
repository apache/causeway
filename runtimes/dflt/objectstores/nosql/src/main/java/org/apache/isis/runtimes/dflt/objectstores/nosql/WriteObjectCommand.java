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
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;


class WriteObjectCommand implements PersistenceCommand {
    private final KeyCreator keyCreator;
    private final ObjectAdapter object;
    private final VersionCreator versionCreator;
    private final boolean isUpdate;

    WriteObjectCommand(boolean isUpdate, KeyCreator keyCreator, VersionCreator versionCreator, ObjectAdapter object) {
        this.isUpdate = isUpdate;
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.object = object;
    }

    @Override
    public void execute(PersistenceCommandContext context) {
        String specName = object.getSpecification().getFullIdentifier();
        StateWriter writer = ((NoSqlCommandContext) context).createStateWriter(specName);
        String key = keyCreator.key(object.getOid());
        writer.writeId(key);
        writeFields(writer, specName, object);
        final String user = IsisContext.getAuthenticationSession().getUserName();

        Version currentVersion = object.getVersion();
        Version newVersion = isUpdate ? versionCreator.nextVersion(currentVersion) : versionCreator.newVersion(user);
        object.setOptimisticLock(newVersion);
        if (newVersion != null) {
            String version = currentVersion == null ? null : versionCreator.versionString(currentVersion);
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

    private void writeFields(StateWriter writer, String specName, ObjectAdapter object) {
        List<ObjectAssociation> associations = object.getSpecification().getAssociations();
        writer.writeType(specName);
        for (ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            ObjectAdapter field = association.get(object);
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

    private void writeAggregatedObject(
            StateWriter writer,
            ObjectAssociation association,
            ObjectAdapter field,
            KeyCreator keyCreator) {
        if (field == null) {
            writer.writeField(association.getId(), null);
        } else if (field.getOid() instanceof AggregatedOid) {
            String specName = field.getSpecification().getFullIdentifier();
            StateWriter aggregateWriter = writer.addAggregate(association.getId());
            aggregateWriter.writeId(((AggregatedOid) field.getOid()).getId());
            writeFields(aggregateWriter, specName, field);
        } else {
            throw new NoSqlStoreException("Object type is inconsistent with it OID - it should have an AggregatedOid: " + field);
        }
    }

    private void writeReference(
            StateWriter writer,
            ObjectAssociation association,
            ObjectAdapter reference,
            KeyCreator keyCreator) {
        if (reference == null) {
            writer.writeField(association.getId(), null);
        } else {
            String key = keyCreator.reference(reference);
            writer.writeField(association.getId(), key);
        }
    }

    private void writeValue(StateWriter writer, ObjectAssociation association, ObjectAdapter value) {
        String data;
        if (value == null) {
            data = null;
        } else {
            EncodableFacet encodeableFacet = value.getSpecification().getFacet(EncodableFacet.class);
            data = encodeableFacet.toEncodedString(value);
        }
        writer.writeField(association.getId(), data);
    }

    private void writeCollection(
            StateWriter writer,
            ObjectAssociation association,
            ObjectAdapter collection,
            KeyCreator keyCreator) {
        CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        if (association.getSpecification().isAggregated()) {
            List<StateWriter> elements = new ArrayList<StateWriter>();
            for (ObjectAdapter element : collectionFacet.iterable(collection)) {
               StateWriter elementWriter = writer.createElementWriter();
               elementWriter.writeId(((AggregatedOid) element.getOid()).getId());
               writeFields(elementWriter, element.getSpecification().getFullIdentifier(), element);
               elements.add(elementWriter);
            }
            writer.writeCollection(association.getId(), elements);
        } else {
            String refs = "";
            for (ObjectAdapter element : collectionFacet.iterable(collection)) {
                if (element.isAggregated()) {
                    throw new DomainModelException(
                            "Can't store an aggregated object within a collection that is not exoected aggregates: " + element
                                    + " (" + collection + ")");
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
        ToString toString = new ToString(this);
        toString.append("spec", object.getSpecification().getFullIdentifier());
        toString.append("oid", object.getOid());
        return toString.toString();
    }
}

