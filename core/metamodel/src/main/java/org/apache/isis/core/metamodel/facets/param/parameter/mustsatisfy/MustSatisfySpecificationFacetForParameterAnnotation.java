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
package org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy;

import java.util.Optional;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacetAbstract;

import lombok.val;

public class MustSatisfySpecificationFacetForParameterAnnotation
extends MustSatisfySpecificationFacetAbstract {

    public static Optional<MustSatisfySpecificationFacet> create(
            final Optional<Parameter> parameterIfAny,
            final FacetHolder holder,
            final FactoryService factoryService) {

        val specifications = parameterIfAny
                .map(Parameter::mustSatisfy)
                .map(classes -> toSpecifications(factoryService, classes))
                .orElseGet(Can::empty);

        return specifications.isEmpty()
                ? Optional.empty()
                : Optional.of(new MustSatisfySpecificationFacetForParameterAnnotation(specifications, holder));
    }

    private MustSatisfySpecificationFacetForParameterAnnotation(
            final Can<Specification> specifications,
            final FacetHolder holder) {
        super(specifications, holder);
    }


}
