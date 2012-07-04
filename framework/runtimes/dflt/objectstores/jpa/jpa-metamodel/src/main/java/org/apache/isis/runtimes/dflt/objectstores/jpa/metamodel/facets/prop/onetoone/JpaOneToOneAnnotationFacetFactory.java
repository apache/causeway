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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone;

import java.lang.reflect.Method;

import javax.persistence.OneToOne;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaOneToOneAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaOneToOneAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final OneToOne annotation = getAnnotation(method, OneToOne.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaOneToOneFacetAnnotation(holder));
        FacetUtil.addFacet(annotation.optional() ? new OptionalFacetDerivedFromJpaOneToOneAnnotation(holder) : new MandatoryFacetDerivedFromJpaOneToOneAnnotation(holder));
        FacetUtil.addFacet(new JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation(annotation.fetch(), holder));
    }

}
