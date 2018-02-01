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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

public class ObjectFixtureService {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFixtureService.class);
    private static final String DATA_FILEPATH = ConfigurationConstants.ROOT + "exploration-objects.file";
    private static final String DEFAULT_FILEPATH = "fixture-data";

    private ObjectFixtureFilePersistor persistor;

    private Set<Object> objects = Sets.newHashSet();


    @PostConstruct
    public void init() {
        persistor = new ObjectFixtureFilePersistor();
        serviceRegistry.injectServicesInto(persistor);
    }

    // {{ title, Id
    public String title() {
        return "Fixture Objects";
    }

    public String getId() {
        return "fixtures";
    }

    public String iconName() {
        return "Fixture";
    }

    // }}

    // {{ action: save
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            describedAs = "Add this object to the set of saved objects"
    )
    @MemberOrder(sequence = "1")
    public void save(final Object object) {
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(object);
        if (adapter.getSpecification().persistability() != Persistability.TRANSIENT) {
            LOG.info("Saving object for fixture: {}", adapter);
            addObjectAndAssociates(adapter);
            saveAll();
        }
    }

    private void addObjectAndAssociates(final ObjectAdapter adapter) {
        if (objects.contains(adapter.getObject())) {
            return;
        }
        objects.add(adapter.getObject());

        final ObjectSpecification adapterSpec = adapter.getSpecification();
        final List<ObjectAssociation> associations = adapterSpec.getAssociations(Contributed.EXCLUDED);
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }

            final ObjectAdapter associatedObject = association.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            final boolean isEmpty = association.isEmpty(adapter, InteractionInitiatedBy.FRAMEWORK);
            if (isEmpty) {
                continue;
            }
            if (association.isOneToManyAssociation()) {
                final CollectionFacet facet = associatedObject.getSpecification().getFacet(CollectionFacet.class);
                for (final ObjectAdapter element : facet.iterable(associatedObject)) {
                    addObjectAndAssociates(element);
                }
            } else if (association.isOneToOneAssociation() && !association.getSpecification().isParseable()) {
                addObjectAndAssociates(associatedObject);
            }
        }
    }

    public String validateSave(final Object object) {
        if (object == this || object instanceof AbstractService) {
            return "Can't add/remove a service";
        }
        return objects.contains(object) ? "This object has already been saved" : null;
    }

    // }}

    // {{ action: remove
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            describedAs = "Remove this object from the set of saved objects"
    )
    @MemberOrder(sequence = "2")
    public void remove(final Object object) {
        objects.remove(object);
        saveAll();
    }

    public String validateRemove(final Object object) {
        if (object == this || object instanceof AbstractService) {
            return "Can't add/remove a service";
        }
        return objects.contains(object) ? null : "Can't remove an object that has not been saved";
    }

    // }}

    // {{ action: all Saved Objects
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            describedAs = "Retrieved all the saved objects"
    )
    @MemberOrder(sequence = "4")
    public Set<Object> allSavedObjects() {
        return objects;
    }

    // }}

    // {{ action: saveAll
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            describedAs = "Save the current state of the saved objects"
    )
    @MemberOrder(sequence = "3")
    public void saveAll() {
        FileWriter out = null;
        try {
            final File file = file(true);
            out = new FileWriter(file);
            persistor.save(objects, out);
        } catch (final IOException e) {
            throw new IsisException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    throw new IsisException(e);
                }
            }
        }
    }

    // }}

    // {{ loadFile (programmtic)
    @Programmatic
    public void loadFile() {
        FileReader reader = null;
        try {
            final File file = file(false);
            reader = new FileReader(file);
            objects = persistor.loadData(reader);
        } catch (final FileNotFoundException e) {
            return;
        } catch (final IOException e) {
            throw new IsisException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new IsisException(e);
                }
            }
        }
    }

    private File file(final boolean createFileIfDoesntExist) throws IOException {
        final String fixturePath = configuration.getString(DATA_FILEPATH, DEFAULT_FILEPATH);
        final File file = new File(fixturePath);
        final File directory = file.getParentFile();
        mkdirIfRequired(directory);
        if (!file.exists() && createFileIfDoesntExist) {
            createFile(file);
        }
        return file;
    }

    private void mkdirIfRequired(final File directory) {
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    private void createFile(final File file) throws IOException {
        file.createNewFile();
    }
    // }}

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

    protected PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    @javax.inject.Inject
    IsisConfiguration configuration;

    @javax.inject.Inject
    ServiceRegistry2 serviceRegistry;

}