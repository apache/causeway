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
package org.apache.isis.viewer.common.model.feature;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;

public interface ParameterUiModel extends ScalarUiModel {

    /** param meta model */
    @Override
    ObjectActionParameter getMetaModel();

    /** param value */
    @NonNull
    ManagedObject getValue();

    /** param value */
    void setValue(ManagedObject paramValue);

    String getCssClass();

    // -- PENDING PARAMETER MODEL

    ParameterNegotiationModel getParameterNegotiationModel();

    // -- SHORTCUTS

    /** param index */
    default int getParameterIndex() {
        return getMetaModel().getParameterIndex();
    }

    @Override
    default int getAutoCompleteMinLength() {
        return hasAutoComplete() ? getMetaModel().getAutoCompleteMinLength() : 0;
    }

    @Override
    default boolean hasChoices() {
        return getMetaModel().hasChoices();
    }

    @Override
    default boolean hasAutoComplete() {
        return getMetaModel().hasAutoComplete();
    }

    @Override
    default ManagedObject getDefault() {
        return getMetaModel().getDefault(getParameterNegotiationModel());
    }

    @Override
    default Can<ManagedObject> getChoices() {
        return getMetaModel().getChoices(getParameterNegotiationModel(), InteractionInitiatedBy.USER);
    }

    @Override
    default Can<ManagedObject> getAutoComplete(final String searchArg) {
        return getMetaModel().getAutoComplete(getParameterNegotiationModel(), searchArg, InteractionInitiatedBy.USER);
    }

    default ActionInteractionHead getPendingParamHead() {
        return getMetaModel().getAction().interactionHead(getOwner());
    }



}
