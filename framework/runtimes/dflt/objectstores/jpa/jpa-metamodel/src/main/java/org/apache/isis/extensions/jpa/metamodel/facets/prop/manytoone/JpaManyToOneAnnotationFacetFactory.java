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
package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import java.lang.reflect.Method;

import javax.persistence.ManyToOne;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaManyToOneAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaManyToOneAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final ManyToOne annotation = getAnnotation(method, ManyToOne.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaManyToOneFacetAnnotation(holder));
        FacetUtil.addFacet(annotation.optional() ? new OptionalFacetDerivedFromJpaManyToOneAnnotation(holder) : new MandatoryFacetDerivedFromJpaManyToOneAnnotation(holder));
        FacetUtil.addFacet(new JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation(annotation.fetch(), holder));
    }

}
