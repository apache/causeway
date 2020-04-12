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

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.disable.DisableDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Decorates a click-able UI component of type {@code <T>} based on an {@link ActionUiModel}.
 * 
 * @see ActionUiModel
 * 
 * @since 2.0.0
 * @param <T> - link component type, native to the viewer
 */
@RequiredArgsConstructor
public class ActionLinkUiComponentDecorator<T> {
    
    private final TooltipDecorator<T> tooltipDecorator;
    private final DisableDecorator<T> disableDecorator;

    public void decorate(T uiComponent, ActionUiModel<?> actionUiModel) {
        val actionMeta = actionUiModel.getActionUiMetaModel();
        //val uiComponent = actionUiModel.getUiComponent();
        
        val disableUiModel = actionMeta.getDisableUiModel();
        disableDecorator.decorate(uiComponent, disableUiModel);
        
        if (disableUiModel.isDisabled()) {
            tooltipDecorator.decorate(uiComponent, TooltipUiModel.ofBody(disableUiModel.getReason().orElse(null)));
        } else {

            if(!_Strings.isNullOrEmpty(actionMeta.getDescription())) {
                tooltipDecorator.decorate(uiComponent, TooltipUiModel.ofBody(actionMeta.getDescription()));
            }
        }

    }

}
