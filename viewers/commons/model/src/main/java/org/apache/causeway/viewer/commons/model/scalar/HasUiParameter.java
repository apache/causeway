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
package org.apache.causeway.viewer.commons.model.scalar;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.NonNull;

@FunctionalInterface
public interface HasUiParameter extends UiParameter {

    UiParameter getUiParameter();

    @Override
    default ObjectActionParameter getObjectFeature() {
        return getUiParameter().getObjectFeature();
    }

    @Override
    default ManagedObject getOwner() {
        return getUiParameter().getOwner();
    }

    @Override
    default @NonNull ManagedObject getValue() {
        return getUiParameter().getValue();
    }

    @Override
    default void setValue(final ManagedObject paramValue) {
        getUiParameter().setValue(paramValue);
    }

    @Override
    default ParameterNegotiationModel getParameterNegotiationModel() {
        return getUiParameter().getParameterNegotiationModel();
    }

    @Override
    default int getAutoCompleteMinLength() {
        return hasAutoComplete() ? getObjectFeature().getAutoCompleteMinLength() : 0;
    }

    @Override
    default boolean hasChoices() {
        return getObjectFeature().hasChoices();
    }

    @Override
    default boolean hasAutoComplete() {
        return getObjectFeature().hasAutoComplete();
    }

    @Override
    default ManagedObject getDefault() {
        return getObjectFeature().getDefault(getParameterNegotiationModel());
    }

    @Override
    default Can<ManagedObject> getChoices() {
        return getObjectFeature().getChoices(getParameterNegotiationModel(), InteractionInitiatedBy.USER);
    }

    @Override
    default Can<ManagedObject> getAutoComplete(final String searchArg) {
        return getObjectFeature().getAutoComplete(getParameterNegotiationModel(), searchArg, InteractionInitiatedBy.USER);
    }

    @Override
    default String getFriendlyName() {
        return getObjectFeature().getFriendlyName(this::getOwner);
    }

    @Override
    default Optional<String> getDescribedAs() {
        return getObjectFeature().getDescription(this::getOwner);
    }

    @Override
    default String getFileAccept() {
        return Facets.fileAccept(getObjectFeature()).orElse(null);
    }

    @Override
    default boolean isRequired() {
        return !getObjectFeature().isOptional();
    }

    @Override
    default ObjectSpecification getScalarTypeSpec() {
        return getObjectFeature().getElementType();
    }

    @Override
    default boolean hasObjectAutoComplete() {
        return Facets.autoCompleteIsPresent(getScalarTypeSpec());
    }

    @Override
    default int getParameterIndex() {
        return getObjectFeature().getParameterIndex();
    }

    @Override
    default String getCssClass() {
        return getUiParameter().getCssClass();
    }

    @Override
    default String getIdentifier() {
        return getUiParameter().getIdentifier();
    }

    @Override
    default boolean whetherHidden() {
        return getUiParameter().whetherHidden();
    }

    @Override
    default String disableReasonIfAny() {
        return getUiParameter().disableReasonIfAny();
    }

    @Override
    default ActionInteractionHead getPendingParamHead() {
        return getObjectFeature().getAction().interactionHead(getUiParameter().getOwner());
    }

    @Override
    default MetaModelContext getMetaModelContext() {
        return getUiParameter().getObjectFeature().getMetaModelContext();
    }

}
