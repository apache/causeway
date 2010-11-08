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


package org.apache.isis.core.progmodel.facets.propparam.typicallength;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;


public class TypicalLengthDerivedFromTypeFacetFactory extends FacetFactoryAbstract {

    public TypicalLengthDerivedFromTypeFacetFactory() {
        super(ObjectFeatureType.PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final Class<?> type = method.getReturnType();
        return addFacetDerivedFromTypeIfPresent(holder, type);
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final Class<?> type = method.getParameterTypes()[paramNum];
        return addFacetDerivedFromTypeIfPresent(holder, type);
    }

    private boolean addFacetDerivedFromTypeIfPresent(final FacetHolder holder, final Class<?> type) {
        final TypicalLengthFacet facet = getTypicalLengthFacet(type);
        if (facet != null) {
            return FacetUtil.addFacet(new TypicalLengthFacetDerivedFromType(facet, holder));
        }
        return false;
    }

    private TypicalLengthFacet getTypicalLengthFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(TypicalLengthFacet.class);
    }

}
