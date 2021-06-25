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
package org.apache.isis.core.metamodel.facets.object.domainservicelayout;

import java.util.Objects;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.NounForms;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacetAbstract;

import lombok.val;

public class NamedFacetForDomainServiceLayoutAnnotation
extends ObjectNamedFacetAbstract {

    public static Optional<ObjectNamedFacet> create(
            final Optional<DomainServiceLayout> domainServiceLayoutIfAny,
            final FacetHolder facetHolder) {

        val serviceNamed = domainServiceLayoutIfAny
                .map(DomainServiceLayout::named)
                .map(_Strings::emptyToNull)
                .filter(Objects::nonNull)
                .orElse(null);

        if(_Strings.isEmpty(serviceNamed)) {
            return Optional.empty();
        }

        val nounForms = NounForms
                .preferredSingular(serviceNamed)
                .build();

        return Optional.of(
                new NamedFacetForDomainServiceLayoutAnnotation(
                            nounForms,
                            facetHolder));
    }

    private NamedFacetForDomainServiceLayoutAnnotation(
            final NounForms nounForms,
            final FacetHolder holder) {
        super(nounForms, holder);
    }


}
