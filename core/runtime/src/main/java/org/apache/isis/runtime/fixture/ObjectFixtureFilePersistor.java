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


package org.apache.isis.runtime.fixture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.fixturesinstaller.FixtureException;

public class ObjectFixtureFilePersistor {

    private static final Logger LOG = Logger.getLogger(ObjectFixtureFilePersistor.class);
    
    private static class SavedObjects {
        private int id = 1;
        private Map<ObjectAdapter, String> idMap = new HashMap<ObjectAdapter, String>();

        public String getId(ObjectAdapter object) {
            String idString = idMap.get(object);
            if (idString == null) { 
                id++;
                idMap.put(object, "" + id);
                idString = "" + id;
            }
            return idString;
        }
    }

    private static class LoadedObjects {
        private Map<String, ObjectAdapter> idMap = new HashMap<String, ObjectAdapter>();
        private final Set<Object> objects;

        public LoadedObjects(Set<Object> objects) {
            this.objects = objects;}

        public ObjectAdapter get(String data) {
            int pos = data.indexOf('#');
            if (pos == -1) {
                throw new FixtureException("load failed - trying to read non-reference data as a reference: " + data);
            }
            String id = data.substring(pos + 1);
            ObjectAdapter object = idMap.get(id);
            if (object == null) {
                String className = data.substring(0, pos);
                ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(className);
                object = IsisContext.getPersistenceSession().createInstance(specification);
                idMap.put(id, object);
                objects.add(object.getObject());
            }
            return object;
        }

    }

    public Set<Object> loadData(Reader reader) throws IOException {
        final Set<Object> objects = new HashSet<Object>();

        BufferedReader buffer = new BufferedReader(reader);
        LoadedObjects loaded = new LoadedObjects(objects);
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
                    if (object != null && !object.isPersistent()) {
                        IsisContext.getPersistenceSession().makePersistent(object);
                    }
                    object = loaded.get(line);
                }
            }

            if (object != null && !object.isPersistent()) {
                IsisContext.getPersistenceSession().makePersistent(object);
            }
        } catch (Exception e) {
            throw new FixtureException("failed to load data at line " + lineNo, e);
        }
        
        return objects;
    }
    
    private void loadFieldData(ObjectAdapter object, LoadedObjects loaded, String line) {
        int pos = line.indexOf(':');
        if (pos == -1) {
            throw new FixtureException("no colon (:) in: " + line.trim());
        }
        String name = line.substring(0, pos).trim();
        String data = line.substring(pos + 1).trim();
        try {
            final ObjectAssociation association = object.getSpecification().getAssociation(name);
            if (data.trim().length() == 0) {
                if (!association.isEmpty(object) && association instanceof OneToOneAssociation) {
                    ((OneToOneAssociation) association).clearAssociation(object);
                }
            } else {
                if (association.isOneToManyAssociation()) {
                    String[] ids = data.split(" ");
                    ObjectAdapter[] elements = new ObjectAdapter[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        elements[i] = loaded.get(ids[i]);
                    }
                    ObjectAdapter collection = association.get(object);
                    CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
                    facet.init(collection, elements);
                } else if (association.getSpecification().isParseable()) {
                    data = data.replaceAll("\\n", "\n");
                    final ParseableFacet facet = association.getSpecification().getFacet(ParseableFacet.class);
                    final ObjectAdapter value = facet.parseTextEntry(null, data);
                    ((OneToOneAssociation) association).initAssociation(object, value);
                } else if (association.isOneToOneAssociation()) {
                    ObjectAdapter value = loaded.get(data);
                    ((OneToOneAssociation) association).initAssociation(object, value);
                }
            }
        } catch (ObjectSpecificationException e) {
            LOG.info("no field for '" + name + "', skipping entry: " + data);
        }
    }

    public void save(Set<Object> objects, Writer out) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        SavedObjects saved = new SavedObjects();
        for (Object object : objects) {
            ObjectAdapter adapter = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(object);
            saveData(writer, adapter, saved);
        }
        out.close();
    }

    private void saveData(PrintWriter writer, ObjectAdapter adapter, SavedObjects saved) {
        String id = saved.getId(adapter);
        writer.println(adapter.getSpecification().getFullName() + "#" + id);

        ObjectSpecification adapterSpec = adapter.getSpecification();
        final ObjectAssociation[] associations = adapterSpec.getAssociations();
        for (ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            
            final ObjectAdapter associatedObject = association.get(adapter);
            final boolean isEmpty = association.isEmpty(adapter);
            final String associationId = association.getId();
            writer.write("  " + associationId + ": ");
            if (isEmpty) {
                writer.println();
                continue;
            }

            if (association.isOneToManyAssociation()) {
                CollectionFacet facet = associatedObject.getSpecification().getFacet(CollectionFacet.class);
                for (ObjectAdapter element : facet.iterable(associatedObject)) {
                    String refId = saved.getId(element);
                    String cls = element.getSpecification().getFullName();
                    writer.print(cls + "#" + refId + " ");
                }
                writer.println();
            } else if (association.getSpecification().isParseable()) {
                final ParseableFacet facet = associatedObject.getSpecification().getFacet(ParseableFacet.class);
                String encodedValue = facet.parseableTitle(associatedObject);
                encodedValue = encodedValue.replaceAll("\n", "\\n");
                writer.println(encodedValue);
            } else if (association.isOneToOneAssociation()) {
                String refId = saved.getId(associatedObject);
                String cls = associatedObject.getSpecification().getFullName();
                writer.println(cls + "#" + refId);
            }
        }
    }

}


