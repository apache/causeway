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
package org.apache.causeway.core.metamodel.services.metamodel;

import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.metamodel.v2.Action;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.Facet;
import org.apache.causeway.schema.metamodel.v2.FacetAttr;
import org.apache.causeway.schema.metamodel.v2.Param;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class ShadowedFactetAttributeAnnotator implements MetaModelAnnotator {

    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private final ExporterConfig config;

    @Override
    public void annotate(final DomainClassDto domainClass, final ObjectSpecification specification) {}
    @Override
    public void annotate(final Action actionType, final ObjectAction action) {}
    @Override
    public void annotate(final Param parameterType, final ObjectActionParameter parameter) {}
    @Override
    public void annotate(final Property propertyType, final OneToOneAssociation property) {}
    @Override
    public void annotate(final Collection collectionType, final OneToManyAssociation collection) {}

    @Override
    public void annotate(final Facet facetType, final org.apache.causeway.core.metamodel.facetapi.Facet facet) {
        facet.getSharedFacetRanking()
        .ifPresent(ranking->{
            ranking.getTopRank(facet.facetType())
            .stream()
            // skip the winner, as its not shadowed
            .skip(1)
            //.filter(shadowedFacet->shadowedFacet.equals(facet))
            .forEach(shadowedFacet->{
                _Util.visitNonNullAttributes(shadowedFacet, (attributeName, str)->{
                    if(attributeName.equals("precedence")) {
                        return; // skip
                    }
                    addAttributeAnnotation(facetType, attributeName, String.format("'%s' from %s)",
                            str,
                            config().abbrev(shadowedFacet.getClass())));
                });
            });
        });
    }

    // -- HELPER

    private void addAttributeAnnotation(final Facet facetType, final String attributeName, final String annotation) {
        lookupByName(facetType, attributeName)
        .ifPresent(facetAttr->{
            createAnnotation(facetAttr, "@shadowed", annotation);
            //createAnnotation(facetAttr, "@title.prefixParentWithExclamation", "");
        });
    }

    private Optional<FacetAttr> lookupByName(final Facet facetType, final String attributeName) {
        return _NullSafe.stream(facetType.getAttr())
        .filter(a->Objects.equals(attributeName, a.getName()))
        .findFirst();
    }

}
