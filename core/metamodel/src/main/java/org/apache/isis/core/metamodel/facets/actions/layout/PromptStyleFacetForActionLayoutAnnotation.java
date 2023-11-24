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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.internal.base._Optionals;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;

public class PromptStyleFacetForActionLayoutAnnotation
extends PromptStyleFacetAbstract {

    private final PromptStyle promptStyle;

    public PromptStyleFacetForActionLayoutAnnotation(final PromptStyle promptStyle, final FacetHolder holder) {
        super( holder );
        this.promptStyle = promptStyle;
    }

    public static Optional<PromptStyleFacet> create(
            final Optional<ActionLayout> actionLayoutIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        return _Optionals.<PromptStyleFacet>orNullable(

        actionLayoutIfAny
        .map(ActionLayout::promptStyle)
        .filter(promptStyle -> promptStyle != PromptStyle.NOT_SPECIFIED)
        .map(promptStyle -> {

            switch (promptStyle) {
            case DIALOG:
            case DIALOG_MODAL:
            case DIALOG_SIDEBAR:
            case INLINE:
            case INLINE_AS_IF_EDIT:
                return new PromptStyleFacetForActionLayoutAnnotation(promptStyle, holder);

            case AS_CONFIGURED:

                // do not replace
                if (holder.containsNonFallbackFacet(PromptStyleFacet.class)) {
                    return null;
                }

                promptStyle = configuration.getViewer().getWicket().getPromptStyle();
                return new PromptStyleFacetAsConfigured(promptStyle, holder);
            default:
                throw new IllegalStateException("promptStyle '" + promptStyle + "' not recognised");
            }

        })

        ,

        () -> {
            // do not replace
            if (holder.containsNonFallbackFacet(PromptStyleFacet.class)) {
                return null;
            }

            PromptStyle promptStyle = configuration.getViewer().getWicket().getPromptStyle();
            return new PromptStyleFacetAsConfigured(promptStyle, holder);
        }

        );

    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("promptStyle", promptStyle);
    }

}
