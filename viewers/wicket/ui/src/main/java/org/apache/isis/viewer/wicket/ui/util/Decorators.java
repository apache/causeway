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
import org.apache.wicket.markup.html.form.Button;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.action.decorator.ActionUiDecorator;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmDecorator;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.danger.DangerDecorator;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingDecorator;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;
import org.apache.isis.viewer.common.model.decorator.icon.IconDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingUiModel;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;

import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

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
    @Getter(lazy = true) private final static Danger danger = new Danger();


    @Getter(lazy = true) private final static IconDecoratorWkt icon = new IconDecoratorWkt();
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

    public final static class Disable implements DisablingDecorator<Component> {
        @Override
        public void decorate(Component uiComponent, DisablingUiModel disableUiModel) {

            val tooltipUiModel = TooltipUiModel.ofBody(disableUiModel.getReason());
            getTooltip().decorate(uiComponent, tooltipUiModel);

            uiComponent.add(new CssClassAppender("disabled"));
            uiComponent.setEnabled(false);

        }
    }

    public final static class Prototyping implements PrototypingDecorator<Component, Component> {
        @Override
        public Component decorate(Component uiComponent, PrototypingUiModel prototypingUiModel) {
            uiComponent.add(new CssClassAppender("prototype"));
            return uiComponent;
        }
    }

    public final static class Confirm implements ConfirmDecorator<Component> {
        @Override
        public void decorate(Component uiComponent, ConfirmUiModel confirmUiModel) {

            val confirmationConfig = new ConfirmationConfig()
                    .withTitle(confirmUiModel.getTitle())
                    .withBtnOkLabel(confirmUiModel.getOkLabel())
                    .withBtnCancelLabel(confirmUiModel.getCancelLabel())
                    .withBtnOkClass("btn btn-danger")
                    .withBtnCancelClass("btn btn-secondary")
                    .withPlacement(Placement.valueOf(confirmUiModel.getPlacement().name().toLowerCase()));

            uiComponent.add(new ConfirmationBehavior(confirmationConfig));

            if(uiComponent instanceof Button) {
                // ensure dialog ok buttons receive the danger style as well
                // don't care if expressed twice
                Decorators.getDanger().decorate(uiComponent);
            }

        }
    }

    public final static class Danger implements DangerDecorator<Component> {
        @Override
        public void decorate(Component uiComponent) {
            //if(uiComponent instanceof Button) {
                uiComponent.add(new CssClassAppender("btn-danger"));
            //}
        }
    }

    public final static class IconDecoratorWkt implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(fontAwesome.isPresent()) {
                uiComponent.add(new CssClassFaBehavior(fontAwesome.get()));
            }
            return uiComponent;
        }
    }

    public final static class MissingIconDecorator implements IconDecorator<Component, Component> {
        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(!fontAwesome.isPresent()) {
                uiComponent.add(new CssClassAppender("menuLinkSpacer"));
            }
            return uiComponent;
        }
    }

    // -- ADVANCED DECORATOR CLASSES

    public final static class ActionLink extends ActionUiDecorator<Component> {

        public ActionLink() {
            super(getTooltip(), getDisable(), getConfirm(), getPrototyping(), getIcon());
        }

        //TODO this is yet the result of refactoring the logic originating from the wicket viewer
        //I'm not happy with this yet: this code decorates 2 UI components at once which is confusing
        //also is not generic enough, because wicket still needs to override this in order to decorate
        //even another UI component
        private <T extends Component> void commonDecorateMenuItem(
                final T uiComponent, // UI component #1
                final LinkAndLabel actionUiModel,
                final TranslationService translationService) {

            val actionLinkUiComponent = actionUiModel.getUiComponent(); // UI component #2
            val actionMeta = actionUiModel.getActionUiMetaModel();

            actionMeta.getDisableUiModel().ifPresent(disableUiModel->{
                getDisableDecorator().decorate(uiComponent, disableUiModel);
                getTooltipDecorator().decorate(uiComponent, TooltipUiModel.ofBody(disableUiModel.getReason()));
            });

            if (!actionMeta.getDisableUiModel().isPresent()) {

                if(!_Strings.isNullOrEmpty(actionMeta.getDescription())) {
                    getTooltipDecorator().decorate(uiComponent, TooltipUiModel.ofBody(actionMeta.getDescription()));
                }

                //XXX ISIS-1626, confirmation dialog for no-parameter menu actions
                if (actionMeta.isRequiresImmediateConfirmation()) {

                    val confirmUiModel = ConfirmUiModel.ofAreYouSure(translationService, ConfirmUiModel.Placement.BOTTOM);
                    getConfirmDecorator().decorate(actionLinkUiComponent, confirmUiModel);

                }

            }

            if (actionMeta.isPrototyping()) {
                getPrototypingDecorator().decorate(actionLinkUiComponent, PrototypingUiModel.of(actionMeta));
            }

        }


        public void decorateMenuItem(
                final Component uiComponent,
                final LinkAndLabel actionUiModel,
                final TranslationService translationService) {

            addCssClassForAction(uiComponent, actionUiModel);

            commonDecorateMenuItem(uiComponent, actionUiModel, translationService);

            val actionMeta = actionUiModel.getActionUiMetaModel();
            val actionLinkUiComponent = actionUiModel.getUiComponent();

            String cssClass = actionMeta.getCssClass();
            if (!_Strings.isNullOrEmpty(cssClass)) {
                actionLinkUiComponent.add(new CssClassAppender(cssClass));
            }

        }

        private void addCssClassForAction(Component uiComponent, LinkAndLabel actionUiModel) {
            val actionMeta = actionUiModel.getActionUiMetaModel();
            uiComponent.add(new CssClassAppender("isis-"
                    + CssClassAppender.asCssStyle(actionMeta.getActionIdentifier())));
        }


    }

}
