/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.core.metamodel.facets.object.domainobjectlayout;

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetSimple;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethod;

public class CssClassFacetForDomainObjectLayoutAnnotation extends CssClassFacetSimple {

    public static Optional<CssClassFacetForDomainObjectLayoutAnnotation> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final FacetHolder holder) {

        CssClassFacet facet = holder.getFacet(CssClassFacet.class);
        // this is a bit hacky, explicitly checking whether a different implementation is already added.
        // normally we would just re-order the list of facet factories in ProgrammingModelsFacetJava5, however in
        // this case @DomainObjectLayout is responsible for two different variations of CssClassFacet, either as
        // a result of the cssClass attribute, but also as a result of the cssClassUiEvent.  The former has lower
        // "priority" to the cssClass() method, but the latter has *higher* priority.  Hence the special casing
        // that is required here.
        if(facet != null && facet instanceof CssClassFacetMethod) {
            // don't overwrite
            return null;
        }
        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::cssClass)
                .filter(_Strings::isNotEmpty)
                .map(cssClass -> new CssClassFacetForDomainObjectLayoutAnnotation(cssClass, holder));
    }

    private CssClassFacetForDomainObjectLayoutAnnotation(
            final String value,
            final FacetHolder holder) {
        super(value, holder);
    }
}
