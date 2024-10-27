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
package org.apache.causeway.core.metamodel.facets.members.cssclass.annotprop;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacetSimple;

public class CssClassFacetOnActionFromConfiguredRegex extends CssClassFacetSimple {

    public static Optional<CssClassFacetOnActionFromConfiguredRegex> create(
            final String name, final FacetHolder holder) {
        return cssIfAnyFor(name, holder.getConfiguration())
            .map(css->new CssClassFacetOnActionFromConfiguredRegex(css, holder));
    }

    private CssClassFacetOnActionFromConfiguredRegex(final String css, final FacetHolder holder) {
        super(css, holder, Precedence.INFERRED); // inferred from config, if not specified otherwise
    }

    // -- HELPER

    private static Optional<String> cssIfAnyFor(
            final String name, final CausewayConfiguration causewayConfiguration) {

        var cssClassByPattern = causewayConfiguration.getApplib().getAnnotation().getActionLayout()
                .getCssClass().getPatternsAsMap();

        for (Map.Entry<Pattern, String> entry : cssClassByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String cssClass = entry.getValue();
            if(pattern.matcher(name).matches()) {
                return Optional.ofNullable(cssClass);
            }
        }
        return Optional.empty();
    }
}
