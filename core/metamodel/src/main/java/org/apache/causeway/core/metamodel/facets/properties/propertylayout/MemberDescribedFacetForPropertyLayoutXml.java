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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacetWithStaticTextAbstract;

import lombok.Getter;
import lombok.experimental.Accessors;

public class MemberDescribedFacetForPropertyLayoutXml
extends MemberDescribedFacetWithStaticTextAbstract
implements QualifiedFacet {

    public static Optional<MemberDescribedFacet> create(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        return Optional.ofNullable(propertyLayoutData)
            .map(PropertyLayoutData::getDescribedAs)
            .filter(_Strings::isNotEmpty)
            .map(describedAs->
                new MemberDescribedFacetForPropertyLayoutXml(describedAs, holder, precedence, qualifier));
    }

    @Getter(onMethod_ = @Override) @Accessors(fluent = true, makeFinal = true)
    private final @Nullable String qualifier;

    private MemberDescribedFacetForPropertyLayoutXml(
            final String described,
            final FacetHolder holder,
            final Precedence precedence,
            final @Nullable String qualifier) {
        super(described, holder, precedence);
        this.qualifier = qualifier;
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }
}
