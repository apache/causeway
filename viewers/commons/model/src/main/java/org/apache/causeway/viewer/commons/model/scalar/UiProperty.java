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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public interface UiProperty extends UiScalar {

    /** prop meta model */
    @Override
    OneToOneAssociation getObjectFeature();

    // -- PENDING PROPERTY VALUE MODEL

    PropertyNegotiationModel getPendingPropertyModel();

    default ManagedProperty getManagedProperty() {
        return getPendingPropertyModel().getManagedProperty();
    }

    // -- SHORTCUTS

    @Override
    default String getIdentifier() {
        return getObjectFeature().getFeatureIdentifier().getMemberLogicalName();
    }

    @Override
    default String getCssClass() {
        return getObjectFeature().getCssClass("causeway-");
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
        return getObjectFeature().getDefault(getOwner());
    }

    @Override
    default Can<ManagedObject> getChoices() {
        return getObjectFeature().getChoices(getOwner(), InteractionInitiatedBy.USER);
    }

    @Override
    default Can<ManagedObject> getAutoComplete(final String searchArg) {
        return getObjectFeature().getAutoComplete(getOwner(), searchArg, InteractionInitiatedBy.USER);
    }

    @Override
    default boolean whetherHidden() {
        return getPendingPropertyModel().whetherHidden();
    }

    @Override
    default String disableReasonIfAny() {
        return getPendingPropertyModel().disableReasonIfAny();
    }

}
