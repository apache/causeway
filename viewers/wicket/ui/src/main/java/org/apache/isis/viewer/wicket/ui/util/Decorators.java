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
package org.apache.isis.viewer.wicket.ui.util;

import java.util.Optional;

import org.apache.wicket.Component;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.viewer.common.model.action.ActionLinkUiComponentDecorator;
import org.apache.isis.viewer.common.model.action.ActionUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmDecorator;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.disable.DisableDecorator;
import org.apache.isis.viewer.common.model.decorator.disable.DisableUiModel;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeUiModel;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;

import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * 
 */
@UtilityClass
public class Decorators {

    // -- BASIC DECORATORS
    @Getter(lazy = true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) private final static Disable disable = new Disable();
    @Getter(lazy = true) private final static Prototyping prototyping = new Prototyping();
    @Getter(lazy = true) private final static Confirm confirm = new Confirm();
    
    @Getter(lazy = true) private final static IconDecorator icon = new IconDecorator();
    @Getter(lazy = true) private final static MissingIconDecorator missingIcon = new MissingIconDecorator();
    
    // -- ADVANCED DECORATORS
    
    @Getter(lazy = true) private final static ActionLink actionLink = new ActionLink();
    
    // -- BASIC DECORATOR CLASSES 
    
    public final static class Tooltip implements TooltipDecorator<Component> {
        @Override
        public void decorate(Component uiComponent, TooltipUiModel tooltipUiModel) {
            Tooltips.addTooltip(uiComponent, tooltipUiModel);
        }
    }
    
    public final static class Disable implements DisableDecorator<Component> {
        @Override
        public void decorate(Component uiComponent, DisableUiModel disableUiModel) {
            if (disableUiModel.isDisabled()) {
                
                disableUiModel.getReason()
                    .map(TooltipUiModel::ofBody)
                    .ifPresent(tooltipUiModel->getTooltip().decorate(uiComponent, tooltipUiModel));
                
                uiComponent.add(new CssClassAppender("disabled"));
                uiComponent.setEnabled(false);
            }
        }
    }
    
    public final static class Prototyping implements PrototypingDecorator<Component> {
        @Override
        public void decorate(Component uiComponent) {
            uiComponent.add(new CssClassAppender("prototype"));
        }
    }
    
    public final static class Confirm implements ConfirmDecorator<Component> {
        @Override
        public void decorate(Component uiComponent, ConfirmUiModel confirmUiModel) {
            Confirmations.addConfirmationDialog(uiComponent, confirmUiModel);
        }
    }
    
    public final static class IconDecorator implements FontAwesomeDecorator<Component> {
        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(fontAwesome.isPresent()) {
                uiComponent.add(new CssClassFaBehavior(fontAwesome.get()));
            } 
            return uiComponent;
        }
    }

    public final static class MissingIconDecorator implements FontAwesomeDecorator<Component> {
        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(!fontAwesome.isPresent()) {
                uiComponent.add(new CssClassAppender("menuLinkSpacer"));
            }
            return uiComponent;
        }
    }
    
    // -- ADVANCED DECORATOR CLASSES
    
    public final static class ActionLink extends ActionLinkUiComponentDecorator<Component> {

        public ActionLink() {
            super(getTooltip(), getDisable(), getConfirm());
        }

        @Override
        public void decorate(
                final TranslationService translationService, 
                final Component uiComponent,
                final ActionUiModel<? extends Component> actionUiModel) {
            addCssClassForAction(uiComponent, actionUiModel);
            super.decorate(translationService, uiComponent, actionUiModel);
        }
        
        private void addCssClassForAction(Component uiComponent, ActionUiModel<?> actionUiModel) {
            val actionMeta = actionUiModel.getActionUiMetaModel();
            uiComponent.add(new CssClassAppender("isis-" 
                    + CssClassAppender.asCssStyle(actionMeta.getActionIdentifier())));
        }
        
        
    }
    
}
