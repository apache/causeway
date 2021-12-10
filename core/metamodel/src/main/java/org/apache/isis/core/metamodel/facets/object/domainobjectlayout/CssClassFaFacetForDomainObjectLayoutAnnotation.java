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
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaStaticFacetAbstract;

public class CssClassFaFacetForDomainObjectLayoutAnnotation
extends CssClassFaStaticFacetAbstract {

    public static Optional<CssClassFaFacet> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final FacetHolder holder) {

        class Annot {
            private Annot(final DomainObjectLayout domainObjectLayout) {
                this.cssClassFa = _Strings.emptyToNull(domainObjectLayout.cssClassFa());
                this.cssClassFaPosition = domainObjectLayout.cssClassFaPosition();
            }
            String cssClassFa;
            CssClassFaPosition cssClassFaPosition;
        }

        return domainObjectLayoutIfAny
                .map(Annot::new)
                .filter(a -> a.cssClassFa != null )
                .map(a -> new CssClassFaFacetForDomainObjectLayoutAnnotation(
                        a.cssClassFa, a.cssClassFaPosition, holder));
    }

    public CssClassFaFacetForDomainObjectLayoutAnnotation(
            final String value,
            final CssClassFaPosition position, //NOSONAR false positive: method IS used in create()/stream().map()
            final FacetHolder holder) {
        super(value, position, holder);
    }
}
