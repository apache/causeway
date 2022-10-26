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

import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacetSimple;

public class CssClassFacetForPropertyLayoutXml
extends CssClassFacetSimple {

    public static Optional<CssClassFacet> create(
            final PropertyLayoutData propertyLayout,
            final FacetHolder holder) {
        if(propertyLayout == null) {
            return Optional.empty();
        }
        final String cssClass = _Strings.emptyToNull(propertyLayout.getCssClass());
        return cssClass != null
                ? Optional.of(new CssClassFacetForPropertyLayoutXml(cssClass, holder))
                : Optional.empty();
    }

    private CssClassFacetForPropertyLayoutXml(final String value, final FacetHolder holder) {
        super(value, holder);
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

}
