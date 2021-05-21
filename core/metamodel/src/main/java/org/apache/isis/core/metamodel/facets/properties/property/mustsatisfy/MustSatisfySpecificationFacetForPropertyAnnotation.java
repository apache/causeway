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

package org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacetAbstract;

import lombok.val;

public class MustSatisfySpecificationFacetForPropertyAnnotation extends MustSatisfySpecificationFacetAbstract {

    public static Facet create(
            final Optional<Property> propertyIfAny,
            final FacetHolder holder,
            final FactoryService factoryService) {

        val specifications = propertyIfAny
                .map(Property::mustSatisfy)
                .map(classes -> toSpecifications(factoryService, classes))
                .orElse(Collections.emptyList());

        return specifications.size() > 0
                ? new MustSatisfySpecificationFacetForPropertyAnnotation(specifications, holder)
                        : null;
    }

    private MustSatisfySpecificationFacetForPropertyAnnotation(final List<Specification> specifications, final FacetHolder holder) {
        super(specifications, holder);
    }


}
