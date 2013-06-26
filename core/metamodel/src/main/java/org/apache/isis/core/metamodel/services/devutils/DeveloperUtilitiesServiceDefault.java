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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.devutils.DeveloperUtilitiesService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class DeveloperUtilitiesServiceDefault implements DeveloperUtilitiesService, SpecificationLoaderAware {

    private final MimeType mimeTypeTextCsv;

    public DeveloperUtilitiesServiceDefault() {
        try {
            mimeTypeTextCsv = new MimeType("text", "csv");
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    @ActionSemantics(Of.SAFE)
    public Clob downloadMetaModel() {
        
        final Collection<ObjectSpecification> specifications = specificationLoader.allSpecifications();
        
        final List<MetaModelRow> rows = Lists.newArrayList();
        for (ObjectSpecification spec : specifications) {
            if(exclude(spec)) {
                continue;
            }
            final List<ObjectAssociation> properties = spec.getAssociations(ObjectAssociationFilters.PROPERTIES);
            for (ObjectAssociation property : properties) {
                final OneToOneAssociation otoa = (OneToOneAssociation)property;
                if(exclude(otoa)) {
                    continue;
                }
                rows.add(new MetaModelRow(spec, otoa));
            }
            final List<ObjectAssociation> associations = spec.getAssociations(ObjectAssociationFilters.COLLECTIONS);
            for (ObjectAssociation collection : associations) {
                final OneToManyAssociation otma = (OneToManyAssociation)collection;
                if(exclude(otma)) {
                    continue;
                }
                rows.add(new MetaModelRow(spec, otma));
            }
            final List<ObjectAction> actions = spec.getObjectActions(Contributed.INCLUDED);
            for (ObjectAction action : actions) {
                if(exclude(action)) {
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
        return className.startsWith("java") || className.startsWith("org.joda") || className.startsWith("org.apache.isis");
    }

    private SpecificationLoader specificationLoader;
    
    @Override
    @Programmatic
    public void setSpecificationLookup(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }


}
