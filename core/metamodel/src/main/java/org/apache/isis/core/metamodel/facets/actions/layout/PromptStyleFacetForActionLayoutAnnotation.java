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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Map;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleConfiguration;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetFallBackToInline;

public class PromptStyleFacetForActionLayoutAnnotation extends PromptStyleFacetAbstract {

    private final PromptStyle promptStyle;

    public PromptStyleFacetForActionLayoutAnnotation(final PromptStyle promptStyle, final FacetHolder holder) {
        super( holder );
        this.promptStyle = promptStyle;
    }

    public static PromptStyleFacet create(
            final ActionLayout actionLayout,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        PromptStyle promptStyle = actionLayout != null? actionLayout.promptStyle() : null;

        if(promptStyle == null) {
            if (holder.containsDoOpNotDerivedFacet(PromptStyleFacet.class)) {
                // do not replace
                return null;
            }

            return new PromptStyleFacetFallBackToInline(holder);
        } else {

            switch (promptStyle) {
                case DIALOG:
                case INLINE:
                case INLINE_AS_IF_EDIT:
                    return new PromptStyleFacetForActionLayoutAnnotation(promptStyle, holder);

                case AS_CONFIGURED:

                    // do not replace
                    if (holder.containsDoOpFacet(PromptStyleFacet.class)) {
                        return null;
                    }

                    promptStyle = PromptStyleConfiguration.parse(configuration);
                    return new PromptStyleFacetAsConfigured(promptStyle, holder);

                default:

                    // do not replace
                    if (holder.containsDoOpFacet(PromptStyleFacet.class)) {
                        return null;
                    }

                    promptStyle = PromptStyleConfiguration.parse(configuration);
                    return new PromptStyleFacetAsConfigured(promptStyle, holder);
            }
        }

    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("promptStyle", promptStyle);
    }

}
