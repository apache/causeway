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
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public interface PropertyUiModel extends ScalarUiModel {

    /** prop meta model */
    @Override
    OneToOneAssociation getMetaModel();

    // -- PENDING PROPERTY VALUE MODEL

    PropertyNegotiationModel getPendingPropertyModel();

    // -- SHORTCUTS

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
        return getMetaModel().getDefault(getOwner());
    }

    @Override
    default Can<ManagedObject> getChoices() {
        return getMetaModel().getChoices(getOwner(), InteractionInitiatedBy.USER);
    }

    @Override
    default Can<ManagedObject> getAutoComplete(final String searchArg) {
        return getMetaModel().getAutoComplete(getOwner(), searchArg, InteractionInitiatedBy.USER);
    }

}
