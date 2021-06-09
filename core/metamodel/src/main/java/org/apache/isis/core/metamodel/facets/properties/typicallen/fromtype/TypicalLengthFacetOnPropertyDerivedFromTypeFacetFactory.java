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

package org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory extends FacetFactoryAbstract {

    public TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Class<?> type = processMethodContext.getMethod().getReturnType();
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        addFacetDerivedFromTypeIfPresent(facetHolder, type);
    }

    private void addFacetDerivedFromTypeIfPresent(final FacetHolder holder, final Class<?> type) {
        final TypicalLengthFacet facet = getTypicalLengthFacet(type);
        if (facet != null) {
            FacetUtil.addFacetIfPresent(new TypicalLengthFacetOnPropertyDerivedFromType(facet, holder));
        }
    }

    private TypicalLengthFacet getTypicalLengthFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(TypicalLengthFacet.class);
    }

}
