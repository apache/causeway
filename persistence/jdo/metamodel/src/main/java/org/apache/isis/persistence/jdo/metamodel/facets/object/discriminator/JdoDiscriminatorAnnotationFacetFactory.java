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

package org.apache.isis.persistence.jdo.metamodel.facets.object.discriminator;

import javax.inject.Inject;
import javax.jdo.annotations.Discriminator;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.core.metamodel.facets.object.logicaltype.classname.LogicalTypeFacetInferredFromClassName;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

public class JdoDiscriminatorAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final JdoFacetContext jdoFacetContext;

    @Inject
    public JdoDiscriminatorAnnotationFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.jdoFacetContext = jdoFacetContext;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = processClassContext.synthesizeOnType(Discriminator.class).orElse(null);
        if (annotation == null) {
            return;
        }
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final String annotationValue = annotation.value();
        final LogicalTypeFacet logicalTypeFacet; // non-null
        if (!_Strings.isNullOrEmpty(annotationValue)) {
            logicalTypeFacet = new LogicalTypeFacetInferredFromJdoDiscriminatorValueAnnotation(
                        LogicalType.eager(cls, annotationValue),
                        facetHolder);
        } else {
            val substitute = classSubstitutorRegistry.getSubstitution(cls);
            if(substitute.isNeverIntrospect()) {
                return;
            }

            val substituted = substitute.apply(cls);
            logicalTypeFacet = new LogicalTypeFacetInferredFromClassName(
                            LogicalType.eager(substituted, substituted.getCanonicalName()),
                            facetHolder);

        }
        FacetUtil.addFacet(logicalTypeFacet);
    }


    @Override
    public void process(final ProcessClassContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = processClassContext.synthesizeOnType(Discriminator.class).orElse(null);
        if (annotation == null) {
            return;
        }
        String annotationValueAttribute = annotation.value();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacet(new JdoDiscriminatorFacetDefault(annotationValueAttribute, facetHolder));
    }

}
