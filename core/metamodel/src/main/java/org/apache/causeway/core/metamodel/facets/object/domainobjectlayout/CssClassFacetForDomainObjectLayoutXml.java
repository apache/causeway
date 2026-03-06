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
package org.apache.causeway.core.metamodel.facets.object.domainobjectlayout;

import java.util.Optional;

import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacetSimple;
import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

public class CssClassFacetForDomainObjectLayoutXml
extends CssClassFacetSimple
implements QualifiedFacet {

    public static Optional<CssClassFacet> create(
            final DomainObjectLayoutData domainObjectLayout,
            final FacetHolder holder,
            final Facet.Precedence precedence,
            final @Nullable String qualifier) {
        if(domainObjectLayout == null)
            return Optional.empty();
        final String cssClass = _Strings.emptyToNull(domainObjectLayout.getCssClass());
        return cssClass != null
            ? Optional.of(new CssClassFacetForDomainObjectLayoutXml(cssClass, holder, precedence, qualifier))
            : Optional.empty();
    }

    @Getter(onMethod_ = @Override) @Accessors(fluent = true, makeFinal = true)
    private final @Nullable String qualifier;

    private CssClassFacetForDomainObjectLayoutXml(
            final String value,
            final FacetHolder holder,
            final Facet.Precedence precedence,
            final @Nullable String qualifier) {
        super(value, holder, precedence);
        this.qualifier = qualifier;
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

}
