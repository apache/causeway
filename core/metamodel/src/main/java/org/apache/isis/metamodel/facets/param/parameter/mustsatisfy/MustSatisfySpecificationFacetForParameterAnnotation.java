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

package org.apache.isis.metamodel.facets.param.parameter.mustsatisfy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacetAbstract;

public class MustSatisfySpecificationFacetForParameterAnnotation extends MustSatisfySpecificationFacetAbstract {

    public static Facet create(
            final List<Parameter> parameters,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        List<Specification> specifications = parameters.stream()
                .map(Parameter::mustSatisfy)
                .flatMap(classes ->
                Arrays.stream(classes)
                .map(MustSatisfySpecificationFacetAbstract::newSpecificationElseNull)
                .filter(Objects::nonNull)
                        )
                .collect(Collectors.toList());
        return specifications.size() > 0
                ? new MustSatisfySpecificationFacetForParameterAnnotation(specifications, holder, servicesInjector)
                        : null;
    }

    private MustSatisfySpecificationFacetForParameterAnnotation(
            final List<Specification> specifications,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(specifications, holder, servicesInjector);
    }

}
