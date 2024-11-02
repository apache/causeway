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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.decorators.ActionDecorators.ActionDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.ConfirmDecorator;
import org.apache.causeway.viewer.commons.model.decorators.ConfirmDecorator.ConfirmDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.DangerDecorator;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator;
import org.apache.causeway.viewer.commons.model.decorators.FormLabelDecorator;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator.TooltipDecorationModel;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.FontAwesomeBehavior;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;
import org.apache.causeway.viewer.wicket.ui.util.BootstrapConstants.ButtonSemantics;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

@UtilityClass
public class WktDecorators {

    // -- BASIC DECORATORS
    @Getter(lazy = true) @Accessors(fluent=true) private final static FormLabel formLabel = new FormLabel();
    @Getter(lazy = true) @Accessors(fluent=true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) @Accessors(fluent=true) private final static Disable disable = new Disable();
    @Getter(lazy = true) @Accessors(fluent=true) private final static Prototyping prototyping = new Prototyping();
    @Getter(lazy = true) @Accessors(fluent=true) private final static Confirm confirm = new Confirm();
    @Getter(lazy = true) @Accessors(fluent=true) private final static Danger danger = new Danger();
    @Getter(lazy = true) @Accessors(fluent=true) private final static FaIcon faIcon = new FaIcon();
    @Getter(lazy = true) @Accessors(fluent=true) private final static MissingIcon missingIcon = new MissingIcon();

    // -- BASIC DECORATOR CLASSES

    public final static class FormLabel implements FormLabelDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent, final FormLabelDecorationModel decorationModel) {
            if(decorationModel.isMandatoryIndicatorShown()) {
                Wkt.cssAppend(uiComponent, "mandatory");
            }
            uiComponent.setEscapeModelStrings(true);
        }
    }

    public final static class Tooltip implements TooltipDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent, final TooltipDecorationModel decorationModel) {
            WktTooltips.addTooltip(uiComponent, decorationModel);
        }
    }

    public final static class Disable implements DisablingDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent, final DisablingDecorationModel decorationModel) {
            var tooltipDecorationModel = TooltipDecorationModel
                    .ofBody(UiPlacementDirection.BOTTOM, decorationModel.reason());
            tooltip().decorate(uiComponent, tooltipDecorationModel);

            Wkt.cssAppend(uiComponent, "disabled");
            uiComponent.setEnabled(false);
        }
    }

    public final static class Prototyping implements PrototypingDecorator<Component, Component> {
        @Override
        public Component decorate(final Component uiComponent, final PrototypingDecorationModel decorationModel) {
            Wkt.cssAppend(uiComponent, "prototype");
            return uiComponent;
        }
    }

    public final static class Confirm implements ConfirmDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent, final ConfirmDecorationModel decorationModel) {
            var confirmationConfig = new ConfirmationConfig()
                    .withTitle(decorationModel.title())
                    .withBtnOkLabel(decorationModel.okLabel())
                    .withBtnCancelLabel(decorationModel.cancelLabel())
                    .withBtnOkClass(ButtonSemantics.DANGER.fullButtonCss())
                    .withBtnCancelClass(ButtonSemantics.SECONDARY.fullButtonCss())
                    .withPlacement(Placement.valueOf(decorationModel.placement().name().toLowerCase()));

            Wkt.behaviorAddConfirm(uiComponent, confirmationConfig);

            if(uiComponent instanceof Button) {
                // ensure dialog ok buttons receive the danger style as well
                // don't care if expressed twice
                WktDecorators.danger().decorate(uiComponent);
            }
        }
    }

    public final static class Danger implements DangerDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent) {
            Wkt.cssAppend(uiComponent, ButtonSemantics.DANGER.buttonDefaultCss());
        }
    }

    public final static class FaIcon implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(final Component uiComponent, final Optional<FontAwesomeLayers> faLayers) {
            if(faLayers.isPresent()) {
                uiComponent.add(new FontAwesomeBehavior(faLayers.get()));
            }
            return uiComponent;
        }
    }

    public final static class MissingIcon implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(final Component uiComponent, final Optional<FontAwesomeLayers> faLayers) {
            if(faLayers.isEmpty()) {
                Wkt.cssAppend(uiComponent, "menuLinkSpacer");
            }
            return uiComponent;
        }
    }

    // -- ACTION DECORATION
    
    public void decorateMenuAction(
            AjaxLink<ManagedObject> actionLink,
            Component tooltipReceiver,
            Label actionLabel,
            ActionDecorationModel decorationModel) {
        
        decorationModel.disabling()
            .ifPresentOrElse(disableUiModel->{
                //DISABLE
                disable().decorate(tooltipReceiver, disableUiModel);
                tooltip().decorate(tooltipReceiver,
                        TooltipDecorationModel.ofBody(UiPlacementDirection.BOTTOM, disableUiModel.reason()));
            }, ()->{
                //DESCRIBE
                decorationModel
                    .describedAs()
                    .ifPresent(describedAs->
                        tooltip()
                            .decorate(tooltipReceiver,
                                    TooltipDecorationModel.ofBody(UiPlacementDirection.BOTTOM, describedAs)));
                
                //{CAUSEWAY-1626] confirmation dialog for no-parameter menu actions
                if (decorationModel.isImmediateConfirmationRequired()) {
                    var confirmUiModel = ConfirmDecorationModel.areYouSure(UiPlacementDirection.BOTTOM);
                    confirm().decorate(actionLink, confirmUiModel);
                }
                
            });
        
        //CSS
        Wkt.cssAppend(tooltipReceiver, decorationModel.featureIdentifier());
        
        decorateActionCommon(actionLink, actionLabel, decorationModel);
    }
    
    /**
     * For rendering actions within action-link panels or drop-downs.
     */
    public void decorateActionLink(
            final AjaxLink<ManagedObject> actionLink,
            final Component tooltipReceiver, 
            final Label actionLabel,
            final ActionDecorationModel decorationModel) {
        
        var action = decorationModel.action();
        
        WktTooltips.addTooltip(tooltipReceiver, decorationModel.disabling().isPresent()
                ? ((ActionLink) actionLink).getReasonDisabledIfAny()
                : decorationModel.describedAs().orElse(null));
        
        if (action.getSemantics().isAreYouSure()) {
            if(action.getParameterCount()==0) {
                if (decorationModel.disabling().isEmpty()) {
                    var confirmUiModel = ConfirmDecorationModel
                            .areYouSure(UiPlacementDirection.BOTTOM);
                    WktDecorators.confirm().decorate(actionLink, confirmUiModel);
                }
            }
            // ensure links receive the danger style
            // don't care if expressed twice
            WktDecorators.danger().decorate(actionLink);
        } else {
            Wkt.cssAppend(actionLink, decorationModel.isLowerVisualRank()
                    ? ButtonSemantics.SECONDARY.buttonOutlineCss()
                    : ButtonSemantics.SECONDARY.buttonDefaultCss());
        }
        
        decorateActionCommon(actionLink, actionLabel, decorationModel);
    }

    // -- ACTION DECORATION HELPER
    
    private void decorateActionCommon(
            AjaxLink<ManagedObject> actionLink,
            Label actionLabel, 
            ActionDecorationModel decorationModel) {
        // ellipsis
        if(decorationModel.isBoundToDialog()) {
            var wicketConfig = MetaModelContext.instanceElseFail().getConfiguration().getViewer().getWicket();
            if(wicketConfig.isActionIndicationWhenBoundToDialog()) {
                var ellipsifiedModel = actionLabel.getDefaultModel().combineWith(Model.of("…"), (a, b)->a + b);
                actionLabel.setDefaultModel(ellipsifiedModel);
            }
        }
        // CSS
        Wkt.cssAppend(actionLink, decorationModel.featureIdentifier());
        decorationModel.additionalCssClass()
            .ifPresent(cssClass->Wkt.cssAppend(actionLink, cssClass));
        // blob/clob
        if (ObjectAction.Util.returnsBlobOrClob(decorationModel.action())) {
            Wkt.cssAppend(actionLink, "noVeil");           
        }
        // prototyping
        decorationModel.prototyping().ifPresent(protoDecModel->{
            prototyping().decorate(actionLink, protoDecModel);
        });
        // fa-icon
        var faLayers = decorationModel.fontAwesomeLayers();
        faIcon().decorate(actionLabel, faLayers);
        if(decorationModel.isMenuItem()) {
            missingIcon().decorate(actionLink, faLayers);    
        }
    }
    
}
