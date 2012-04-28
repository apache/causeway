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

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.StateWriter;
import org.apache.isis.runtimes.dflt.objectstores.nosql.encryption.DataEncryption;
import org.apache.isis.runtimes.dflt.objectstores.nosql.keys.KeyCreator;
import org.apache.isis.runtimes.dflt.objectstores.nosql.versions.VersionCreator;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

class WriteObjectCommand implements PersistenceCommand {
    
    private final KeyCreator keyCreator;
    private final ObjectAdapter object;
    private final VersionCreator versionCreator;
    private final DataEncryption dataEncrypter;
    private final boolean isUpdate;

    WriteObjectCommand(final boolean isUpdate, final KeyCreator keyCreator, final VersionCreator versionCreator, final DataEncryption dataEncrypter, final ObjectAdapter object) {
        this.isUpdate = isUpdate;
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.dataEncrypter = dataEncrypter;
        this.object = object;
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        final NoSqlCommandContext noSqlCommandContext = (NoSqlCommandContext) context;
        
        final String specName = object.getSpecification().getFullIdentifier();
        final StateWriter writer = noSqlCommandContext.createStateWriter(specName);
        final String key = keyCreator.key(object.getOid());
        writer.writeId(key);
        writeFields(writer, specName, object);
        final String user = getAuthenticationSession().getUserName();

        final Version currentVersion = object.getVersion();
        final Version newVersion = isUpdate ? versionCreator.nextVersion(currentVersion) : versionCreator.newVersion(user);
        object.setVersion(newVersion);
        if (newVersion != null) {
            final String version = currentVersion == null ? null : versionCreator.versionString(currentVersion);
            writer.writeVersion(version, versionCreator.versionString(newVersion));
            writer.writeUser(newVersion.getUser());
            writer.writeTime(versionCreator.timeString(newVersion));
            writer.writeEncryptionType(dataEncrypter.getType());
        }

        if (isUpdate) {
            noSqlCommandContext.update(writer);
        } else {
            noSqlCommandContext.insert(writer);
        }
    }

    private void writeFields(final StateWriter writer, final String specName, final ObjectAdapter adapter) {
        final List<ObjectAssociation> associations = adapter.getSpecification().getAssociations();
        writer.writeType(specName);
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            final ObjectAdapter fieldAdapter = association.get(adapter);
            if (association.isOneToManyAssociation()) {
                final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) association;
                final ObjectAdapter collectionAdapter = fieldAdapter; // to explain
                writeCollection(writer, oneToManyAssociation, collectionAdapter, keyCreator);
            } else { 
                final OneToOneAssociation oneToOneAssociation = (OneToOneAssociation) association;
                final ObjectAdapter propertyAdapter = fieldAdapter; // to explain
                writeProperty(writer, oneToOneAssociation, propertyAdapter);
            }
        }
    }

    private void writeProperty(final StateWriter writer, final OneToOneAssociation oneToOneAssociation, final ObjectAdapter propertyAdapter) {
        if (oneToOneAssociation.getSpecification().isValue()) {
            final ObjectAdapter valueAdapter = propertyAdapter; // to explain
            writeValueProperty(writer, oneToOneAssociation, valueAdapter);
        } else { 
            final ObjectAdapter referencedAdapter = propertyAdapter; // to explain 
            writeReferenceProperty(writer, oneToOneAssociation, referencedAdapter);
        }
    }

    private void writeValueProperty(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter valueAdapter) {
        String data;
        if (valueAdapter == null) {
            data = null;
        } else {
            final EncodableFacet encodeableFacet = valueAdapter.getSpecification().getFacet(EncodableFacet.class);
            data = encodeableFacet.toEncodedString(valueAdapter);
            data = dataEncrypter.encrypt(data);
        }
        writer.writeField(otoa.getId(), data);
    }

    private void writeReferenceProperty(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter referencedAdapter) {
        if (otoa.getSpecification().isParented()) {
            writeReferencedAsAggregated(writer, otoa, referencedAdapter, keyCreator);
        } else {
            writeReference(writer, otoa, referencedAdapter, keyCreator);
        }
    }

    private void writeReferencedAsAggregated(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter referencedAdapter, final KeyCreator keyCreator) {
        if (referencedAdapter == null) {
            writer.writeField(otoa.getId(), null);
            return;
        } 
        final Oid referencedOid = referencedAdapter.getOid();
        if (!(referencedOid instanceof AggregatedOid)) {
            throw new NoSqlStoreException("Object type is inconsistent with it OID - it should have an AggregatedOid: " + referencedAdapter);
        } 
        final AggregatedOid aggregatedOid = (AggregatedOid) referencedOid;
        
        final String associationId = otoa.getId();
        final StateWriter aggregateWriter = writer.addAggregate(associationId);
        aggregateWriter.writeId(aggregatedOid.getLocalId());
        
        final String specName = referencedAdapter.getSpecification().getFullIdentifier();
        writeFields(aggregateWriter, specName, referencedAdapter);
    }

    private void writeReference(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter referencedAdapter, final KeyCreator keyCreator) {
        final String key = keyCreator.reference(referencedAdapter);
        writer.writeField(association.getId(), key);
    }

    private void writeCollection(final StateWriter writer, final OneToManyAssociation association, final ObjectAdapter collectionAdapter, final KeyCreator keyCreator) {
        if (association.getSpecification().isParented()) {
            writeCollectionOfAggregated(writer, association, collectionAdapter);
        } else {
            writeCollectionOfReferences(writer, association, collectionAdapter, keyCreator);
        }
    }

    private void writeCollectionOfAggregated(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter collectionAdapter) {
        final List<StateWriter> elementWriters = Lists.newArrayList();
        final CollectionFacet collectionFacet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        for (final ObjectAdapter referencedAdapter : collectionFacet.iterable(collectionAdapter)) {
            final AggregatedOid elementOid = (AggregatedOid) referencedAdapter.getOid();
            final StateWriter elementWriter = writer.createElementWriter();
            elementWriter.writeId(elementOid.getLocalId());
            writeFields(elementWriter, referencedAdapter.getSpecification().getFullIdentifier(), referencedAdapter);
            elementWriters.add(elementWriter);
        }
        writer.writeCollection(association.getId(), elementWriters);
    }

    private void writeCollectionOfReferences(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter collectionAdapter, final KeyCreator keyCreator) {
        final CollectionFacet collectionFacet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        
        final StringBuilder buf = new StringBuilder();
        for (final ObjectAdapter elementAdapter : collectionFacet.iterable(collectionAdapter)) {
            if (elementAdapter.isParented()) {
                throw new DomainModelException("Can't store an aggregated object within a collection that is not expected aggregates: " + elementAdapter + " (" + collectionAdapter + ")");
            }
            buf.append(keyCreator.reference(elementAdapter)).append("|");
        }
        if (buf.length() > 0) {
            writer.writeField(association.getId(), buf.toString());
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
    
    
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
