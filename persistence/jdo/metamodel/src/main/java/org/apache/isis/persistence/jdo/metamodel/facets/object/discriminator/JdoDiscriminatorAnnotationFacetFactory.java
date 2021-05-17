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
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectTypeFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectTypeFacetDerivedFromClassName;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.Setter;
import lombok.val;

public class JdoDiscriminatorAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectTypeFacetFactory {

    @Inject private ClassSubstitutorRegistry classSubstitutorRegistry;
    @Inject @Setter private JdoFacetContext jdoFacetContext;

    public JdoDiscriminatorAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = Annotations.getAnnotation(cls, Discriminator.class);
        if (annotation == null) {
            return;
        }
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final String annotationValue = annotation.value();
        final ObjectTypeFacet facet;
        if (!_Strings.isNullOrEmpty(annotationValue)) {
            facet = new ObjectTypeFacetInferredFromJdoDiscriminatorValueAnnotation(
                        LogicalType.eager(cls, annotationValue),
                        facetHolder);
        } else {
            val substitute = classSubstitutorRegistry.getSubstitution(cls);
            if(substitute.isNeverIntrospect()) {
                return;
            }

            val substituted = substitute.apply(cls);
            facet = new ObjectTypeFacetDerivedFromClassName(
                            LogicalType.eager(substituted, substituted.getCanonicalName()),
                            facetHolder);

        }
        FacetUtil.addFacet(facet);
    }


    @Override
    public void process(ProcessClassContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = Annotations.getAnnotation(processClassContext.getCls(), Discriminator.class);
        if (annotation == null) {
            return;
        }
        String annotationValueAttribute = annotation.value();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacet(new JdoDiscriminatorFacetDefault(annotationValueAttribute, facetHolder));
    }

}
