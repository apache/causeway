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

import java.util.List;
import com.google.common.collect.Lists;
import org.apache.isis.applib.annotation.MustSatisfy;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

/**
 * @deprecated
 */
@Deprecated
public class MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty extends MustSatisfySpecificationFacetAbstract {

    public static Facet create(
            final MustSatisfy annotation,
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        if (annotation == null) {
            return null;
        }
        final Class<?>[] values = annotation.value();
        final List<Specification> specifications = specificationsFor(values);
        return specifications.size() > 0 ? new MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty(specifications, holder, servicesInjector) : null;
    }

    private MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty(
            final List<Specification> specifications,
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        super(specifications, holder, servicesInjector);
    }

}
