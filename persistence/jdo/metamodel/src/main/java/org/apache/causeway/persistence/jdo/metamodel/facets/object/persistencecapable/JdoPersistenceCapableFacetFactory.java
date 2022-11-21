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
package org.apache.causeway.persistence.jdo.metamodel.facets.object.persistencecapable;


import javax.inject.Inject;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

/**
 * Implements {@link ObjectTypeFacetFactory} only because is a prereq of {@link DomainObjectAnnotationFacetFactory}.
 */
public class JdoPersistenceCapableFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    private final JdoFacetContext jdoFacetContext;

    @Inject
    public JdoPersistenceCapableFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.jdoFacetContext = jdoFacetContext;
    }

    @Override
    public void process(final ObjectTypeFacetFactory.ProcessObjectTypeContext processClassContext) {

        val cls = processClassContext.getCls();

        // only applies to JDO entities; ignore non enhanced classes
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        if(!processJdoAnnotations(processClassContext)) {
            processJpaAnnotations(processClassContext);
        }
    }

    // -- HELPER

    private boolean processJdoAnnotations(final ProcessObjectTypeContext processClassContext) {
        val cls = processClassContext.getCls();

        val persistenceCapableIfAny = processClassContext.synthesizeOnType(PersistenceCapable.class);
        if (!persistenceCapableIfAny.isPresent()) {
            return false;
        }

        val embeddedOnlyIfAny = processClassContext.synthesizeOnType(EmbeddedOnly.class);
        val facetHolder = processClassContext.getFacetHolder();

        return FacetUtil.addFacetIfPresent(
                JdoPersistenceCapableFacetFromAnnotation
                .createUsingJdo(persistenceCapableIfAny, embeddedOnlyIfAny, cls, facetHolder))
        .map(jdoPersistenceCapableFacet->{

            FacetUtil.addFacet(
                    jdoFacetContext.createEntityFacet(facetHolder, cls));

            return true; // jdoPersistenceCapableFacet was created
        })
        .orElse(false);
    }

    private void processJpaAnnotations(final ProcessObjectTypeContext processClassContext) {
        val entityIfAny = processClassContext.synthesizeOnType(Entity.class);
        if(!entityIfAny.isPresent()) {
            return;
        }

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        //val embeddedOnlyIfAny = processClassContext.synthesizeOnType(Embeddable.class);
        val tableIfAny = processClassContext.synthesizeOnType(Table.class);

        FacetUtil.addFacetIfPresent(
                JdoPersistenceCapableFacetFromAnnotation
                .createUsingJpa(entityIfAny, tableIfAny, cls, facetHolder))
        .ifPresent(jdoPersistenceCapableFacet->{

            FacetUtil.addFacet(
                    jdoFacetContext.createEntityFacet(facetHolder, cls));

        });
    }

}
