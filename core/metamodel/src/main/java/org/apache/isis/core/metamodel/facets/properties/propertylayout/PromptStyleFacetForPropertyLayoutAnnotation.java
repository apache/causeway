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
package org.apache.isis.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.commons.internal.base._Optionals;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;

public class PromptStyleFacetForPropertyLayoutAnnotation
extends PromptStyleFacetAbstract {

    private final PromptStyle promptStyle;

    public PromptStyleFacetForPropertyLayoutAnnotation(final PromptStyle promptStyle, final FacetHolder holder) {
        super( holder );
        this.promptStyle = promptStyle;
    }

    public static Optional<PromptStyleFacet> create(
            final Optional<PropertyLayout> propertyLayoutIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        // guard against member not being a property
        if(holder instanceof FacetedMethod) {
            final FacetedMethod facetedMethod = (FacetedMethod) holder;
            if(facetedMethod.getFeatureType() != FeatureType.PROPERTY) {
                return Optional.empty();
            }
        }

        return _Optionals.orNullable(

            propertyLayoutIfAny
            .map(PropertyLayout::promptStyle)
            .filter(promptStyle -> promptStyle != PromptStyle.NOT_SPECIFIED)
            .map(promptStyle -> {

                switch (promptStyle) {
                case DIALOG:
                case DIALOG_MODAL:
                case DIALOG_SIDEBAR:
                case INLINE:
                    return new PromptStyleFacetForPropertyLayoutAnnotation(promptStyle, holder);
                case INLINE_AS_IF_EDIT:
                    return new PromptStyleFacetForPropertyLayoutAnnotation(PromptStyle.INLINE, holder);

                case AS_CONFIGURED:

                    // do not replace
                    if (holder.containsNonFallbackFacet(PromptStyleFacet.class)) {
                        return null;
                    }

                    promptStyle = configuration.getViewer().getWicket().getPromptStyle();
                    return new PromptStyleFacetAsConfigured(promptStyle, holder);
                default:
                }
                throw new IllegalStateException("promptStyle '" + promptStyle + "' not recognised");
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
