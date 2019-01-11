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

package org.apache.isis.core.metamodel.facets.object.cssclassfa.annotation;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.CssClassFaFacetForViewModelLayoutAnnotation;

public class CssClassFaFacetForViewModelLayoutFactory extends CssClassFaFacetAbstract {

    public static CssClassFaFacet create(final ViewModelLayout viewModelLayout, final FacetHolder holder) {
        if (viewModelLayout == null) {
            return null;
        }
        final String cssClassFa = Strings.emptyToNull(viewModelLayout.cssClassFa());
        final CssClassFaPosition position = CssClassFaPosition.from(viewModelLayout.cssClassFaPosition());
        return cssClassFa != null ? new CssClassFaFacetForViewModelLayoutAnnotation(cssClassFa,
                position,
                holder) : null;
    }

    private CssClassFaFacetForViewModelLayoutFactory(final String value, final CssClassFaPosition position, //NOSONAR false positive; method is used in create()
            final FacetHolder holder) {
        super(value, position, holder);
    }
}
