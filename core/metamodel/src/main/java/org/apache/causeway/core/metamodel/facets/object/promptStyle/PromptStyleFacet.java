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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Provides the prompt style for editing of a property.
 */
public class PromptStyleFacet
   extends FacetAbstract
   implements QualifiedFacet {

   public static PromptStyleFacet compositeValueEdit(final FacetHolder facetHolder) {
       return new PromptStyleFacet("CompositeValueEdit", PromptStyle.INLINE_AS_IF_EDIT, facetHolder){
    	   @Override public Precedence precedence() { return Precedence.SYNTHESIZED; }
       };
   }
   public static PromptStyleFacet asConfgured(final CausewayConfiguration configuration, final FacetHolder facetHolder) {
       return new PromptStyleFacet("Configuration", configuration.viewer().wicket().promptStyle(), facetHolder);
   }
   public static Optional<PromptStyleFacet> createForActionLayoutXml(
           final ActionLayoutData actionLayoutData,
           final ObjectAction objectAction,
           final Precedence precedence,
           @Nullable final String qualifier) {
       return Optional.ofNullable(actionLayoutData)
           .map(ActionLayoutData::getPromptStyle)
           .map(promptStyle->new PromptStyleFacet("ActionLayoutXml", promptStyle, objectAction) {
        	   @Override final public Precedence precedence() { return precedence; }
        	   @Override final public @Nullable String qualifier() { return qualifier; }
        	   @Override final public boolean isObjectTypeSpecific() { return true; }
    	   });
   }
   public static Optional<PromptStyleFacet> createForPropertyLayoutXml(
           final PropertyLayoutData propertyLayoutData,
           final OneToOneAssociation oneToOneAssociation,
           final Precedence precedence,
           @Nullable final String qualifier) {
       return Optional.ofNullable(propertyLayoutData)
           .map(PropertyLayoutData::getPromptStyle)
           .map(promptStyle->new PromptStyleFacet("PropertyLayoutXml", promptStyle, oneToOneAssociation) {
        	   @Override final public Precedence precedence() { return precedence; }
        	   @Override final public @Nullable String qualifier() { return qualifier; }
        	   @Override final public boolean isObjectTypeSpecific() { return true; }
           });
   }

   @Getter @Accessors(fluent = true)
   private final String origin;
   @Getter @Accessors(fluent = true)
   private final PromptStyle value;

   public PromptStyleFacet(
	       final String origin,
	       final PromptStyle value,
	       final FacetHolder facetHolder) {
	   super(PromptStyleFacet.class, facetHolder);
	   this.origin = origin;
	   this.value = value;
   }

   @Override
   public void visitAttributes(final BiConsumer<String, Object> visitor) {
	   super.visitAttributes(visitor);
       visitor.accept("origin", origin);
       visitor.accept("promptStyle", value);
   }

}
