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

package org.apache.isis.objectstore.jdo.metamodel.facets.object.discriminator;

import javax.jdo.annotations.Discriminator;

import com.google.common.base.Strings;

import org.apache.isis.core.metamodel.JdoMetamodelUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassName;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;

public class JdoDiscriminatorAnnotationFacetFactory extends FacetFactoryAbstract {

    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    public JdoDiscriminatorAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = Annotations.getAnnotation(processClassContext.getCls(), Discriminator.class);
        if (annotation == null) {
            return;
        }
        String annotationValueAttribute = annotation.value();
        if(!Strings.isNullOrEmpty(annotationValueAttribute)) {
            FacetUtil.addFacet(new ObjectSpecIdFacetInferredFromJdoDiscriminatorValueAnnotation(annotationValueAttribute, processClassContext.getFacetHolder()));
        } else {
            final FacetHolder facetHolder = processClassContext.getFacetHolder();
            final Class<?> originalClass = processClassContext.getCls();
            final Class<?> substitutedClass = classSubstitutor.getClass(originalClass);
            FacetUtil.addFacet(new ObjectSpecIdFacetDerivedFromClassName(substitutedClass.getCanonicalName(), facetHolder));
        }

        FacetUtil.addFacet(new JdoDiscriminatorFacetDefault(annotationValueAttribute, processClassContext.getFacetHolder()));
    }

}
