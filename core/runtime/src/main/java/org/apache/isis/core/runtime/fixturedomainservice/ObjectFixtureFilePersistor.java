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

package org.apache.isis.core.runtime.fixturedomainservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

public class ObjectFixtureFilePersistor {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFixtureFilePersistor.class);

    public Set<Object> loadData(final Reader reader) throws IOException {
        final Set<Object> objects = new HashSet<Object>();

        final BufferedReader buffer = new BufferedReader(reader);
        final LoadedObjects loaded = new LoadedObjects(objects, specificationLoader, getPersistenceSession());
        String line;
        ObjectAdapter object = null;
        int lineNo = 0;
        try {
            while ((line = buffer.readLine()) != null) {
                lineNo++;
                if (line.trim().startsWith("#")) {
                    continue;
                } else if (line.startsWith("  ")) {
                    loadFieldData(object, loaded, line);
                } else {
                    if (object != null && !object.representsPersistent()) {
                        getPersistenceSession().makePersistentInTransaction(object);
                    }
                    object = loaded.get(line);
                }
            }

            if (object != null && !object.representsPersistent()) {
                getPersistenceSession().makePersistentInTransaction(object);
            }
        } catch (final Exception e) {
            throw new FixtureException("failed to load data at line " + lineNo, e);
        }

        return objects;
    }

    private void loadFieldData(final ObjectAdapter object, final LoadedObjects loaded, final String line) {
        final int pos = line.indexOf(':');
        if (pos == -1) {
            throw new FixtureException("no colon (:) in: " + line.trim());
        }
        final String name = line.substring(0, pos).trim();
        String data = line.substring(pos + 1).trim();
        try {
            final ObjectAssociation association = object.getSpecification().getAssociation(name);
            if (data.trim().length() == 0) {
                if (    association instanceof OneToOneAssociation &&
                        !association.isEmpty(object, InteractionInitiatedBy.FRAMEWORK)) {
                    final OneToOneAssociation property = (OneToOneAssociation) association;
                    property.set(object, null, InteractionInitiatedBy.FRAMEWORK);
                }
            } else {
                if (association.isOneToManyAssociation()) {
                    final String[] ids = data.split(" ");
                    final ObjectAdapter[] elements = new ObjectAdapter[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        elements[i] = loaded.get(ids[i]);
                    }
                    final ObjectAdapter collection = association.get(object, InteractionInitiatedBy.FRAMEWORK);
                    final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
                    facet.init(collection, elements);
                } else if (association.getSpecification().isParseable()) {
                    data = data.replaceAll("\\n", "\n");
                    final ParseableFacet facet = association.getSpecification().getFacet(ParseableFacet.class);
                    final ObjectAdapter value = facet.parseTextEntry(null, data, InteractionInitiatedBy.FRAMEWORK);
                    ((OneToOneAssociation) association).initAssociation(object, value);
                } else if (association.isOneToOneAssociation()) {
                    final ObjectAdapter value = loaded.get(data);
                    ((OneToOneAssociation) association).initAssociation(object, value);
                }
            }
        } catch (final ObjectSpecificationException e) {
            LOG.info("no field for '" + name + "', skipping entry: " + data);
        }
    }

    public void save(final Set<Object> objects, final Writer out) throws IOException {
        final PrintWriter writer = new PrintWriter(out);
        final SavedObjects saved = new SavedObjects();
        for (final Object object : objects) {
            final ObjectAdapter adapter = getPersistenceSession().adapterFor(object);
            saveData(writer, adapter, saved);
        }
        out.close();
    }

    private void saveData(final PrintWriter writer, final ObjectAdapter adapter, final SavedObjects saved) {
        final String id = saved.getId(adapter);
        writer.println(adapter.getSpecification().getFullIdentifier() + "#" + id);

        final ObjectSpecification adapterSpec = adapter.getSpecification();
        final List<ObjectAssociation> associations = adapterSpec.getAssociations(Contributed.EXCLUDED);
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }

            final ObjectAdapter associatedObject = association.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            final boolean isEmpty = association.isEmpty(adapter, InteractionInitiatedBy.FRAMEWORK);
            final String associationId = association.getId();
            writer.write("  " + associationId + ": ");
            if (isEmpty) {
                writer.println();
                continue;
            }

            if (association.isOneToManyAssociation()) {
                final CollectionFacet facet = associatedObject.getSpecification().getFacet(CollectionFacet.class);
                for (final ObjectAdapter element : facet.iterable(associatedObject)) {
                    final String refId = saved.getId(element);
                    final String cls = element.getSpecification().getFullIdentifier();
                    writer.print(cls + "#" + refId + " ");
                }
                writer.println();
            } else if (association.getSpecification().isParseable()) {
                final ParseableFacet facet = associatedObject.getSpecification().getFacet(ParseableFacet.class);
                String encodedValue = facet.parseableTitle(associatedObject);
                encodedValue = encodedValue.replaceAll("\n", "\\n");
                writer.println(encodedValue);
            } else if (association.isOneToOneAssociation()) {
                final String refId = saved.getId(associatedObject);
                final String cls = associatedObject.getSpecification().getFullIdentifier();
                writer.println(cls + "#" + refId);
            }
        }
    }


    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

    protected PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    @javax.inject.Inject
    SpecificationLoader specificationLoader;

}


class LoadedObjects {
    private final Map<String, ObjectAdapter> idMap = Maps.newHashMap();
    private final Set<Object> objects;
    private final SpecificationLoader specificationLoader;
    private final PersistenceSession persistenceSession;

    public LoadedObjects(
            final Set<Object> objects,
            final SpecificationLoader specificationLoader,
            final PersistenceSession persistenceSession) {
        this.objects = objects;
        this.specificationLoader = specificationLoader;
        this.persistenceSession = persistenceSession;
    }

    public ObjectAdapter get(final String data) {
        final int pos = data.indexOf('#');
        if (pos == -1) {
            throw new FixtureException("load failed - trying to read non-reference data as a reference: " + data);
        }
        final String id = data.substring(pos + 1);
        ObjectAdapter object = idMap.get(id);
        if (object == null) {
            final String className = data.substring(0, pos);
            final ObjectSpecification specification = specificationLoader.loadSpecification(className);
            object = persistenceSession.createTransientInstance(specification);
            idMap.put(id, object);
            objects.add(object.getObject());
        }
        return object;
    }
}

class SavedObjects {
    private int id = 1;
    private final Map<ObjectAdapter, String> idMap = new HashMap<ObjectAdapter, String>();

    public String getId(final ObjectAdapter object) {
        String idString = idMap.get(object);
        if (idString == null) {
            id++;
            idMap.put(object, "" + id);
            idString = "" + id;
        }
        return idString;
    }
}

