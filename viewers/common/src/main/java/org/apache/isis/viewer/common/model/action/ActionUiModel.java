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
package org.apache.isis.viewer.common.model.action;

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.HasUiComponent;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @since 2.0.0
 * @param <T> - link component type, native to the viewer
 */
@RequiredArgsConstructor
public class ActionUiModel<T> implements HasUiComponent<T> {

    private final Function<ActionUiModel<T>, T> uiComponentFactory;
    @Getter private final String named;
    @Getter private final ObjectUiModel actionHolder;
    @Getter private final ObjectAction objectAction;
    
    @Getter(onMethod = @__(@Override), lazy = true) private final T uiComponent = uiComponentFactory.apply(this);
    
    public ActionUiMetaModel getActionUiMetaModel() {
        return actionUiMetaModel.get();
    }
    
    @Override
    public String toString() {
        return Optional.ofNullable(named).orElse("") + 
                " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }
    
    // -- VISIBILITY
    
    public boolean isVisible() {
        return isVisible(actionHolder.getManagedObject(), objectAction);
    }
    
    private static boolean isVisible(
            @NonNull final ManagedObject actionHolder, 
            @NonNull final ObjectAction objectAction) {
        
        // check hidden
        if (actionHolder.getSpecification().isHidden()) {
            return false;
        }
        // check visibility
        final Consent visibility = objectAction.isVisible(
                actionHolder,
                InteractionInitiatedBy.USER,
                Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return false;
        }
        return true;
    }
    
    
    // -- HELPER
    
    private final _Lazy<ActionUiMetaModel> actionUiMetaModel = _Lazy.threadSafe(this::createActionUiMetaModel);
    
    private ActionUiMetaModel createActionUiMetaModel() {
        return ActionUiMetaModel.of(actionHolder.getManagedObject(), objectAction);
    }
    
}
