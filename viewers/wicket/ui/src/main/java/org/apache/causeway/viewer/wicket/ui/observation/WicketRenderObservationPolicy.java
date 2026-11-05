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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.util.Optional;

import org.apache.causeway.applib.Identifier;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.PropertyModel;

/**
 * Centralizes the bounded member-level inclusion policy for Wicket render observations.
 *
 * @since 4.0.0
 */
public final class WicketRenderObservationPolicy {

    private WicketRenderObservationPolicy() {}

    public static Optional<WicketRenderObservationDescriptor> propertyDescriptor(
            final UiAttributeWkt attributeModel) {
        if(!(attributeModel instanceof PropertyModel)
                || (attributeModel.getRenderingHint() != RenderingHint.REGULAR
                    && attributeModel.getRenderingHint() != RenderingHint.PARENTED_PROPERTY_COLUMN)) {
            return Optional.empty();
        }
        final var featureId = attributeModel.getMetaModel().getFeatureIdentifier();
        return Optional.of(WicketRenderObservationDescriptor.property(
                featureId.logicalTypeName(),
                featureId.getLogicalIdentityString("#")));
    }

    public static Optional<WicketRenderObservationDescriptor> actionDescriptor(
            final ActionModel actionModel,
            final Where where) {
        if((where != Where.OBJECT_FORMS && where != Where.PARENTED_TABLES && where != Where.ALL_TABLES)
                || actionModel.getAssociatedParameter().isPresent()) {
            return Optional.empty();
        }
        final var featureId = actionIdentifier(actionModel);
        return Optional.of(WicketRenderObservationDescriptor.action(
                featureId.logicalTypeName(),
                featureId.getLogicalIdentityString("#")));
    }

    /** UI contributions already carry a domain identity; retain a safe fallback for physical mixin actions. */
    public static Identifier actionIdentifier(final ActionModel model) {
        var action = model.getAction();
        var id = action.getFeatureIdentifier();
        if (!action.isDeclaredOnMixin()) return id;
        var memberName = action.getProgrammingModel().mixinNamingStrategy()
                .memberId(id.logicalType().correspondingClass());
        return Identifier.actionIdentifier(
                model.getParentUiModel().getTypeOfSpecification().logicalType(),
                memberName, id.memberParameterClassNames());
    }
}
