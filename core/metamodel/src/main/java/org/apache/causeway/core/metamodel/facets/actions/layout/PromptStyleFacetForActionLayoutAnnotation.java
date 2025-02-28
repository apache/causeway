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
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;

public final class PromptStyleFacetForActionLayoutAnnotation
extends PromptStyleFacetAbstract {

    public static Optional<PromptStyleFacet> create(
            final Optional<ActionLayout> actionLayoutIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        return Optional.ofNullable(
            actionLayoutIfAny
                .map(ActionLayout::promptStyle)
                .filter(promptStyle -> promptStyle != PromptStyle.NOT_SPECIFIED)
                .map(promptStyle -> switch (promptStyle) {
                    case DIALOG, DIALOG_MODAL, DIALOG_SIDEBAR, INLINE, INLINE_AS_IF_EDIT->
                        new PromptStyleFacetForActionLayoutAnnotation(promptStyle, holder);
                    case AS_CONFIGURED->
                        holder.containsNonFallbackFacet(PromptStyleFacet.class)
                            ? null // do not replace
                            : new PromptStyleFacetAsConfigured(configuration, holder);
                    case NOT_SPECIFIED -> null; // unexpected code reach
                })
                .orElseGet(() ->
                    // do not replace
                    holder.containsNonFallbackFacet(PromptStyleFacet.class)
                        ? null
                        : new PromptStyleFacetAsConfigured(configuration, holder))
        );
    }

    private final PromptStyle promptStyle;

    private PromptStyleFacetForActionLayoutAnnotation(final PromptStyle promptStyle, final FacetHolder holder) {
        super(holder);
        this.promptStyle = promptStyle;
    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

}
