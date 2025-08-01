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
package org.apache.causeway.core.metamodel.facets.object.promptStyle;

import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

/**
 * Provides the prompt style for editing of a property.
 */
public record PromptStyleFacet(
       @NonNull String origin,
       @NonNull PromptStyle value,
       @NonNull FacetHolder facetHolder,
       Facet.@NonNull Precedence precedence,
       boolean isObjectTypeSpecific
   ) implements Facet {

   public static PromptStyleFacet compositeValueEdit(final FacetHolder facetHolder) {
       return new PromptStyleFacet("CompositeValueEdit", PromptStyle.INLINE_AS_IF_EDIT, facetHolder, Precedence.SYNTHESIZED, false);
   }
   public static PromptStyleFacet asConfgured(final CausewayConfiguration configuration, final FacetHolder facetHolder) {
       return new PromptStyleFacet("Configuration", configuration.viewer().wicket().promptStyle(), facetHolder, Precedence.DEFAULT, false);
   }

   @Override public Class<? extends Facet> facetType() { return getClass(); }
   @Override public Precedence getPrecedence() { return precedence(); }
   @Override public FacetHolder getFacetHolder() { return facetHolder(); }

   public PromptStyleFacet(final String origin, final PromptStyle of, final FacetHolder holder) {
       this(origin, of, holder, Precedence.DEFAULT, false);
   }

   public PromptStyleFacet(final String origin, final PromptStyle of, final FacetHolder holder, final boolean isObjectTypeSpecific) {
       this(origin, of, holder, Precedence.DEFAULT, isObjectTypeSpecific);
   }

   @Override
   public void visitAttributes(final BiConsumer<String, Object> visitor) {
       visitor.accept("origin", origin());
       visitor.accept("precedence", getPrecedence().name());
       visitor.accept("promptStyle", value);
   }

}
