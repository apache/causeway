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

package org.apache.isis.core.progmodel.facets.members.describedas.annotation;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class DescribedAsAnnotationOnMemberFacetFactory extends FacetFactoryAbstract {

    public DescribedAsAnnotationOnMemberFacetFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // look for annotation on the property
        final DescribedAs annotation = Annotations.getAnnotation(processMethodContext.getMethod(), DescribedAs.class);
        DescribedAsFacet facet = create(annotation, processMethodContext.getFacetHolder());
        if (facet != null) {
            FacetUtil.addFacet(facet);
            return;
        }

        // otherwise, look for annotation on the type
        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        final DescribedAsFacet returnTypeDescribedAsFacet = getDescribedAsFacet(returnType);
        if (returnTypeDescribedAsFacet != null) {
            facet = new DescribedAsFacetForMemberDerivedFromType(returnTypeDescribedAsFacet, processMethodContext.getFacetHolder());
            FacetUtil.addFacet(facet);
        }
    }

    private DescribedAsFacet create(final DescribedAs annotation, final FacetHolder holder) {
        return annotation == null ? null : new DescribedAsFacetAnnotationOnMember(annotation.value(), holder);
    }

    private DescribedAsFacet getDescribedAsFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(DescribedAsFacet.class);
    }

}
