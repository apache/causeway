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

package org.apache.isis.core.metamodel.facets.object.domainobjectlayout;

import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public class CssClassFaFacetForDomainObjectXml extends CssClassFaFacetAbstract {

    public static CssClassFaFacet create(final DomainObjectLayoutData domainObjectLayout, final FacetHolder holder) {
        if(domainObjectLayout == null) {
            return null;
        }
        final String cssClassFa = _Strings.emptyToNull(domainObjectLayout.getCssClassFa());
        CssClassFaPosition cssClassFaPosition = CssClassFaPosition.from(domainObjectLayout.getCssClassFaPosition());
        return cssClassFa != null ? new CssClassFaFacetForDomainObjectXml(cssClassFa, cssClassFaPosition, holder) : null;
    }

    private CssClassFaFacetForDomainObjectXml(final String value, final CssClassFaPosition position, final FacetHolder holder) {
        super(value, position, holder);
    }

}
