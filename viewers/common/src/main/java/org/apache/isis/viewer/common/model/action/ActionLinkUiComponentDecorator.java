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

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmDecorator;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.common.model.decorator.disable.DisableDecorator;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;

import lombok.Getter;
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
@Getter
@RequiredArgsConstructor
public class ActionLinkUiComponentDecorator<T> {
    
    private final TooltipDecorator<T> tooltipDecorator;
    private final DisableDecorator<T> disableDecorator;
    private final ConfirmDecorator<T> confirmDecorator;
    private final PrototypingDecorator<T> prototypingDecorator;
    private final FontAwesomeDecorator<T> faDecorator;

    //TODO this is yet the result of refactoring the logic originating from the wicket viewer
    //there is a little design flaw yet: this code decorates 2 UI components at once which is confusing 
    public void decorateMenuItem(
            final T uiComponent, // with wicket this is a menu item component
            final ActionUiModel<? extends T> actionUiModel,
            final TranslationService translationService) {
        
        val actionLinkUiComponent = actionUiModel.getUiComponent();
        val actionMeta = actionUiModel.getActionUiMetaModel();
        
        val disableUiModel = actionMeta.getDisableUiModel();
        disableDecorator.decorate(uiComponent, disableUiModel);
        
        if (disableUiModel.isDisabled()) {
            tooltipDecorator.decorate(uiComponent, TooltipUiModel.ofBody(disableUiModel.getReason().orElse(null)));
            
        } else {

            if(!_Strings.isNullOrEmpty(actionMeta.getDescription())) {
                tooltipDecorator.decorate(uiComponent, TooltipUiModel.ofBody(actionMeta.getDescription()));
            }
            
            //XXX ISIS-1626, confirmation dialog for no-parameter menu actions
            if (actionMeta.isRequiresImmediateConfirmation()) {
                
                val confirmUiModel = ConfirmUiModel.ofAreYouSure(translationService, Placement.BOTTOM);
                confirmDecorator.decorate(actionLinkUiComponent, confirmUiModel);
                
            }
            
        }
        
        if (actionMeta.isPrototyping()) {
            prototypingDecorator.decorate(actionLinkUiComponent);
        }

    }

}
