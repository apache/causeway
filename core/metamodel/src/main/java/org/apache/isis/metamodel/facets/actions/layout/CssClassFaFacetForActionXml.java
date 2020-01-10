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

package org.apache.isis.metamodel.facets.actions.layout;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaPosition;

public class CssClassFaFacetForActionXml extends CssClassFaFacetAbstract {

    public static CssClassFaFacet create(final ActionLayoutData actionLayout, final FacetHolder holder) {
        if(actionLayout == null) {
            return null;
        }
        final String cssClassFa = _Strings.emptyToNull(actionLayout.getCssClassFa());
        CssClassFaPosition cssClassFaPosition = CssClassFaPosition.from(actionLayout.getCssClassFaPosition());
        return cssClassFa != null ? new CssClassFaFacetForActionXml(cssClassFa, cssClassFaPosition, holder) : null;
    }

    private CssClassFaFacetForActionXml(final String value, final CssClassFaPosition position, final FacetHolder holder) {
        super(value, position, holder);
    }

}
