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
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.model.action.decorator.UiActionDecorator;
import org.apache.causeway.viewer.commons.model.decorators.ConfirmDecorator;
import org.apache.causeway.viewer.commons.model.decorators.ConfirmDecorator.ConfirmDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.DangerDecorator;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator;
import org.apache.causeway.viewer.commons.model.decorators.FormLabelDecorator;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator.PrototypingDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator.TooltipDecorationModel;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.causeway.viewer.wicket.ui.util.BootstrapConstants.ButtonSemantics;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameRemover;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;
import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 *
 */
@UtilityClass
public class WktDecorators {

    // -- BASIC DECORATORS
    @Getter(lazy = true) private final static FormLabel formLabel = new FormLabel();
    @Getter(lazy = true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) private final static Disable disable = new Disable();
    @Getter(lazy = true) private final static Prototyping prototyping = new Prototyping();
    @Getter(lazy = true) private final static Confirm confirm = new Confirm();
    @Getter(lazy = true) private final static Danger danger = new Danger();
    @Getter(lazy = true) private final static IconDecoratorWkt icon = new IconDecoratorWkt();
    @Getter(lazy = true) private final static MissingIconDecorator missingIcon = new MissingIconDecorator();

    // -- ADVANCED DECORATORS

    @Getter(lazy = true) private final static ActionLink actionLink = new ActionLink();

    // -- BASIC DECORATOR CLASSES

    public final static class FormLabel implements FormLabelDecorator<Component> {
        @Override
        public void decorate(final Component uiComponent, final FormLabelDecorationModel decorationModel) {
            if(decorationModel.isMandatoryMarker()) {
                Wkt.cssAppend(uiComponent, "mandatory");
            }
            if(_Strings.isNotEmpty(decorationModel.getSuffix())) {
                final IModel<String> labelModel = _Casts.uncheckedCast(uiComponent.getDefaultModel());
                uiComponent.setDefaultModel(Model.of(labelModel.getObject() + decorationModel.getSuffix()));
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
            val tooltipDecorationModel = TooltipDecorationModel
                    .ofBody(UiPlacementDirection.BOTTOM, decorationModel.getReason());
            getTooltip().decorate(uiComponent, tooltipDecorationModel);

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

            val confirmationConfig = new ConfirmationConfig()
                    .withTitle(decorationModel.getTitle())
                    .withBtnOkLabel(decorationModel.getOkLabel())
                    .withBtnCancelLabel(decorationModel.getCancelLabel())
                    .withBtnOkClass(ButtonSemantics.DANGER.fullButtonCss())
                    .withBtnCancelClass(ButtonSemantics.SECONDARY.fullButtonCss())
                    .withPlacement(Placement.valueOf(decorationModel.getPlacement().name().toLowerCase()));

            Wkt.behaviorAddConfirm(uiComponent, confirmationConfig);

            if(uiComponent instanceof Button) {
                // ensure dialog ok buttons receive the danger style as well
                // don't care if expressed twice
                WktDecorators.getDanger().decorate(uiComponent);
            }

        }
    }

    public final static class Danger implements DangerDecorator<Component> {

        private final CssClassNameRemover cssButtonSemanticsRemover =
                ButtonSemantics.createButtonSemanticsRemover();

        @Override
        public void decorate(final Component uiComponent) {

            uiComponent.add(cssButtonSemanticsRemover);

            Wkt.cssAppend(uiComponent, ButtonSemantics.DANGER.buttonDefaultCss());
        }
    }

    public final static class IconDecoratorWkt implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(final Component uiComponent, final Optional<FontAwesomeDecorationModel> fontAwesome) {
            if(fontAwesome.isPresent()) {
                uiComponent.add(new CssClassFaBehavior(fontAwesome.get()));
            }
            return uiComponent;
        }
    }

    public final static class MissingIconDecorator implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(final Component uiComponent, final Optional<FontAwesomeDecorationModel> fontAwesome) {
            if(!fontAwesome.isPresent()) {
                Wkt.cssAppend(uiComponent, "menuLinkSpacer");
            }
            return uiComponent;
        }
    }

    // -- ADVANCED DECORATOR CLASSES

    public final static class ActionLink extends UiActionDecorator<Component> {

        public ActionLink() {
            super(getTooltip(), getDisable(), getConfirm(), getPrototyping(), getIcon());
        }

        //TODO this is yet the result of refactoring the logic originating from the wicket viewer
        //I'm not happy with this yet: this code decorates 2 UI components at once which is confusing
        //also is not generic enough, because wicket still needs to override this in order to decorate
        //even another UI component
        private <T extends Component> void commonDecorateMenuItem(
                final T uiComponent, // UI component #1
                final LinkAndLabel linkAndLabel,
                final TranslationService translationService) {

            val actionLinkUiComponent = linkAndLabel.getUiComponent(); // UI component #2
            val actionMeta = linkAndLabel.getManagedAction().getAction();

            linkAndLabel.getDisableUiModel().ifPresent(disableUiModel->{
                getDisableDecorator().decorate(uiComponent, disableUiModel);
                getTooltipDecorator().decorate(uiComponent,
                        TooltipDecorationModel.ofBody(UiPlacementDirection.BOTTOM, disableUiModel.getReason()));
            });

            if (!linkAndLabel.getDisableUiModel().isPresent()) {

                linkAndLabel
                .getDescription()
                .ifPresent(describedAs->
                    getTooltipDecorator()
                    .decorate(uiComponent,
                            TooltipDecorationModel.ofBody(UiPlacementDirection.BOTTOM, describedAs)));

                //XXX CAUSEWAY-1626, confirmation dialog for no-parameter menu actions
                if (actionMeta.isImmediateConfirmationRequired()) {

                    val confirmUiModel = ConfirmDecorationModel.areYouSure(translationService, UiPlacementDirection.BOTTOM);
                    getConfirmDecorator().decorate(actionLinkUiComponent, confirmUiModel);

                }

            }

            if (actionMeta.isPrototype()) {
                getPrototypingDecorator()
                .decorate(actionLinkUiComponent, PrototypingDecorationModel.of(linkAndLabel.getManagedAction()));
            }

        }

        public void decorateMenuItem(
                final Component uiComponent,
                final LinkAndLabel linkAndLabel,
                final TranslationService translationService) {

            Wkt.cssAppend(uiComponent, linkAndLabel.getFeatureIdentifier());

            commonDecorateMenuItem(uiComponent, linkAndLabel, translationService);

            linkAndLabel.getAdditionalCssClass()
                .ifPresent(cssClass->Wkt.cssAppend(linkAndLabel.getUiComponent(), cssClass));
        }

    }

}
