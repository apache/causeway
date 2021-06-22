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
package org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable;


import javax.inject.Inject;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.object.domainobject.objectspecid.LogicalTypeFacetInferredFromJdoPersistenceCapableAnnotation;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

/**
 * Implements {@link ObjectTypeFacetFactory} only because is a prereq of {@link DomainObjectAnnotationFacetFactory}.
 */
public class JdoPersistenceCapableAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    private final JdoFacetContext jdoFacetContext;

    @Inject
    public JdoPersistenceCapableAnnotationFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.jdoFacetContext = jdoFacetContext;
    }

    @Override
    public void process(final ObjectTypeFacetFactory.ProcessObjectTypeContext processClassContext) {
        val cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        val persistenceCapableIfAny = processClassContext.synthesizeOnType(PersistenceCapable.class);
        if (!persistenceCapableIfAny.isPresent()) {
            return;
        }

        val persistenceCapable = persistenceCapableIfAny.get();

        String annotationSchemaAttribute = persistenceCapable.schema();
        if(_Strings.isNullOrEmpty(annotationSchemaAttribute)) {
            annotationSchemaAttribute = null;
        }
        String annotationTableAttribute = persistenceCapable.table();
        if (_Strings.isNullOrEmpty(annotationTableAttribute)) {
            annotationTableAttribute = cls.getSimpleName();
        }

        val facetHolder = processClassContext.getFacetHolder();

        val embeddedOnlyAttribute = persistenceCapable.embeddedOnly();
        // Whether objects of this type can only be embedded,
        // hence have no ID that binds them to the persistence layer
        final boolean embeddedOnly = Boolean.valueOf(embeddedOnlyAttribute)
                || Annotations.getAnnotation(cls, EmbeddedOnly.class)!=null;

        if(embeddedOnly) {
            // suppress
        } else {

            final IdentityType annotationIdentityType = persistenceCapable.identityType();
            val jdoPersistenceCapableFacet =
            FacetUtil.addFacet(new JdoPersistenceCapableFacetAnnotation(
                    annotationSchemaAttribute,
                    annotationTableAttribute, annotationIdentityType, facetHolder));
            FacetUtil.addFacetIfPresent(LogicalTypeFacetInferredFromJdoPersistenceCapableAnnotation
                    .create(jdoPersistenceCapableFacet, cls, facetHolder));
        }

        return;
    }


}
