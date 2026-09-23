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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;

/**
 * Centralizes the bounded member-level inclusion policy for Wicket render observations.
 *
 * @since 2.2
 */
public final class WicketRenderObservationPolicy {

    private WicketRenderObservationPolicy() {}

    public static Optional<WicketRenderObservationDescriptor> propertyDescriptor(
            final ScalarModel scalarModel) {
        if(!(scalarModel instanceof ScalarPropertyModel)
                || scalarModel.getRenderingHint() != RenderingHint.REGULAR) {
            return Optional.empty();
        }
        final var featureId = scalarModel.getMetaModel().getFeatureIdentifier();
        return Optional.of(WicketRenderObservationDescriptor.property(
                featureId.logicalTypeName(),
                featureId.getLogicalIdentityString("#")));
    }

    public static Optional<WicketRenderObservationDescriptor> actionDescriptor(
            final ActionModel actionModel,
            final Where where) {
        if(where != Where.OBJECT_FORMS || actionModel.getAssociatedParameter().isPresent()) {
            return Optional.empty();
        }
        final var featureId = actionModel.getAction().getFeatureIdentifier();
        return Optional.of(WicketRenderObservationDescriptor.action(
                featureId.logicalTypeName(),
                featureId.getLogicalIdentityString("#")));
    }
}
