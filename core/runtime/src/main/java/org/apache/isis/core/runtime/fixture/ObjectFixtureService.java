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


package org.apache.isis.core.runtime.fixture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.context.IsisContext;


public class ObjectFixtureService {
    private ObjectFixtureFilePersistor persistor = new ObjectFixtureFilePersistor();

    private static final Logger LOG = Logger.getLogger(ObjectFixtureService.class);
    private static final String DATA_FILEPATH = ConfigurationConstants.ROOT + "exploration-objects.file";
    private static final String DEFAULT_FILEPATH = "fixture-data";
    private Set<Object> objects = new HashSet<Object>();

    public String title() {
        return "Fixture Objects";
    }

    public String getId() {
        return "fixtures";
    }

    public String iconName() {
        return "Fixture";
    }

    @DescribedAs("Add this object to the set of saved objects")
    @MemberOrder(sequence = "1")
    @Exploration
    public void save(Object object) {
        ObjectAdapter adapter = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(object);
        if (adapter.getSpecification().persistability() != Persistability.TRANSIENT) {
            LOG.info("Saving object for fixture: " + adapter);
            addObjectAndAssociates(adapter);
            saveAll();
        }
    }

    private void addObjectAndAssociates(ObjectAdapter adapter) {
        if (objects.contains(adapter.getObject())) {
            return;
        }
        objects.add(adapter.getObject());

        ObjectSpecification adapterSpec = adapter.getSpecification();
        final List<ObjectAssociation> associations = adapterSpec.getAssociations();
        for (ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }

            final ObjectAdapter associatedObject = association.get(adapter);
            final boolean isEmpty = association.isEmpty(adapter);
            if (isEmpty) {
                continue;
            }
            if (association.isOneToManyAssociation()) {
                CollectionFacet facet = associatedObject.getSpecification().getFacet(CollectionFacet.class);
                for (ObjectAdapter element : facet.iterable(associatedObject)) {
                    addObjectAndAssociates(element);
                }
            } else if (association.isOneToOneAssociation() && !association.getSpecification().isParseable()) {
                addObjectAndAssociates(associatedObject);
            }
        }
    }

    public String validateSave(Object object) {
        if (object == this || object instanceof AbstractService) {
            return "Can't add/remove a service";
        }
        return objects.contains(object) ? "This object has already been saved" : null;
    }

    @DescribedAs("Remove this object from the set of saved objects")
    @MemberOrder(sequence = "2")
    @Exploration
    public void remove(Object object) {
        objects.remove(object);
        saveAll();
    }

    public String validateRemove(Object object) {
        if (object == this || object instanceof AbstractService) {
            return "Can't add/remove a service";
        }
        return objects.contains(object) ? null : "Can't remove an object that has not been saved";
    }

    @DescribedAs("Retrieved all the saved objects")
    @MemberOrder(sequence = "4")
    @Exploration
    public Set<Object> allSavedObjects() {
        return objects;
    }

    @DescribedAs("Save the current state of the saved objects")
    @MemberOrder(sequence = "3")
    @Exploration
    public void saveAll() {
        FileWriter out = null;
        try {
            File file = file(true);
            out = new FileWriter(file);
            persistor.save(objects, out);
        } catch (IOException e) {
            throw new IsisException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new IsisException(e);
                }
            }
        }
    }

    @Hidden
    public void loadFile() {
        FileReader reader = null;
        try {
            File file = file(false);
            reader = new FileReader(file);
            objects = persistor.loadData(reader);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            throw new IsisException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IsisException(e);
                }
            }
        }
    }

    private File file(boolean createFileIfDoesntExist) throws IOException {
        String fixturePath = IsisContext.getConfiguration().getString(DATA_FILEPATH, DEFAULT_FILEPATH);
        File file = new File(fixturePath);
        File directory = file.getParentFile();
        mkdirIfRequired(directory);
        if (!file.exists() && createFileIfDoesntExist) {
            createFile(file);
        }
        return file;
    }

    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTIC")
    private void mkdirIfRequired(File directory) {
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void createFile(File file) throws IOException {
        file.createNewFile();
    }

}

