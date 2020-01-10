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

package org.apache.isis.core.metamodel.facets.object.domainobject.choices;

import java.util.Optional;

import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.choices.ChoicesFacetFromBoundedAbstract;

public class ChoicesFacetForDomainObjectAnnotation extends ChoicesFacetFromBoundedAbstract {

    public static Facet create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder facetHolder) {

        return domainObjectIfAny
                .map(DomainObject::bounding)
                .filter(bounding -> bounding != Bounding.NOT_SPECIFIED)
                .map(bounding -> bounding == Bounding.BOUNDED
                ? new ChoicesFacetForDomainObjectAnnotation(facetHolder)
                        : null).orElse(null);
    }

    private ChoicesFacetForDomainObjectAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

}
