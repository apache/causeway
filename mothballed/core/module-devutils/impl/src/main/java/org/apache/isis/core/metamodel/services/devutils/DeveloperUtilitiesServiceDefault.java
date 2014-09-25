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
package org.apache.isis.core.metamodel.services.devutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.devutils.DeveloperUtilitiesService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.spec.feature.*;

public class DeveloperUtilitiesServiceDefault implements DeveloperUtilitiesService, SpecificationLoaderSpiAware, AdapterManagerAware {


    private final MimeType mimeTypeTextCsv;
    private final MimeType mimeTypeApplicationZip;
    private final MimeType mimeTypeApplicationJson;

    public DeveloperUtilitiesServiceDefault() {
        try {
            mimeTypeTextCsv = new MimeType("text", "csv");
            mimeTypeApplicationJson = new MimeType("application", "jzon");
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }

    // //////////////////////////////////////

    
    @Override
    public Clob downloadMetaModel() {

        final Collection<ObjectSpecification> specifications = specificationLoader.allSpecifications();

        final List<MetaModelRow> rows = Lists.newArrayList();
        for (ObjectSpecification spec : specifications) {
            if (exclude(spec)) {
                continue;
            }
            final List<ObjectAssociation> properties = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
            for (ObjectAssociation property : properties) {
                final OneToOneAssociation otoa = (OneToOneAssociation) property;
                if (exclude(otoa)) {
                    continue;
                }
                rows.add(new MetaModelRow(spec, otoa));
            }
            final List<ObjectAssociation> associations = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.COLLECTIONS);
            for (ObjectAssociation collection : associations) {
                final OneToManyAssociation otma = (OneToManyAssociation) collection;
                if (exclude(otma)) {
                    continue;
                }
                rows.add(new MetaModelRow(spec, otma));
            }
            final List<ObjectAction> actions = spec.getObjectActions(Contributed.INCLUDED);
            for (ObjectAction action : actions) {
                if (exclude(action)) {
                    continue;
                }
                rows.add(new MetaModelRow(spec, action));
            }
        }

        Collections.sort(rows);

        final StringBuilder buf = new StringBuilder();
        buf.append(MetaModelRow.header()).append("\n");
        for (MetaModelRow row : rows) {
            buf.append(row.asTextCsv()).append("\n");
        }
        return new Clob("metamodel.csv", mimeTypeTextCsv, buf.toString().toCharArray());
    }

    protected boolean exclude(OneToOneAssociation property) {
        return false;
    }

    protected boolean exclude(OneToManyAssociation collection) {
        return false;
    }

    protected boolean exclude(ObjectAction action) {
        return false;
    }

    protected boolean exclude(ObjectSpecification spec) {
        return isBuiltIn(spec) || spec.isAbstract();
    }

    protected boolean isBuiltIn(ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }

    // //////////////////////////////////////
    
    @Override
    public void refreshServices() {
        Collection<ObjectSpecification> specifications = Lists.newArrayList(specificationLoader.allSpecifications());
        for (ObjectSpecification objectSpec : specifications) {
            if(objectSpec.isService()){
                specificationLoader.invalidateCache(objectSpec.getCorrespondingClass());
            }
        }
    }

    // //////////////////////////////////////

    @Override
    public Object refreshLayout(Object domainObject) {
        specificationLoader.invalidateCacheFor(domainObject);
        return domainObject;
    }

    // //////////////////////////////////////
    
    @Override
    public Clob downloadLayout(Object domainObject) {
        
        final ObjectAdapter adapterFor = adapterManager.adapterFor(domainObject);
        final ObjectSpecification objectSpec = adapterFor.getSpecification();
        
        final LayoutMetadataReaderFromJson propertiesReader = new LayoutMetadataReaderFromJson();
        final String json = propertiesReader.asJson(objectSpec);
        
        return new Clob(objectSpec.getShortIdentifier() +".layout.json", mimeTypeApplicationJson, json);
    }

    // //////////////////////////////////////

    @Override
    public Blob downloadLayouts() {
        final LayoutMetadataReaderFromJson propertiesReader = new LayoutMetadataReaderFromJson();
        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        final Collection<ObjectSpecification> domainObjectSpecs = Collections2.filter(allSpecs, new Predicate<ObjectSpecification>(){
            @Override
            public boolean apply(ObjectSpecification input) {
                return  !input.isAbstract() && 
                        !input.isService() && 
                        !input.isValue() && 
                        !input.isParentedOrFreeCollection();
            }});
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            OutputStreamWriter writer = new OutputStreamWriter(zos);
            for (ObjectSpecification objectSpec : domainObjectSpecs) {
                zos.putNextEntry(new ZipEntry(zipEntryNameFor(objectSpec)));
                writer.write(propertiesReader.asJson(objectSpec));
                writer.flush();
                zos.closeEntry();
            }
            writer.close();
            return new Blob("layouts.zip", mimeTypeApplicationZip, baos.toByteArray());
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip of layouts", ex);
        }
    }

    private static String zipEntryNameFor(ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.json";
    }


    // //////////////////////////////////////

    private SpecificationLoaderSpi specificationLoader;
    private AdapterManager adapterManager;

    @Programmatic
    @Override
    public void setSpecificationLoaderSpi(SpecificationLoaderSpi specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    @Override
    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }


}
