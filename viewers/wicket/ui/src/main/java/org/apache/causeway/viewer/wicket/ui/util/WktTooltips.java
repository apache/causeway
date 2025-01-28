/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.viewer.wicket.ui.util;

import java.time.Duration;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.viewer.web.TextMode;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator.TooltipDecorationModel;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;
import org.apache.causeway.viewer.wicket.ui.util.ExtendedPopoverConfig.PopoverBoundary;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.OpenTrigger;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.jquery.Config;

@UtilityClass
public class WktTooltips {

    /**
     * Adds popover behavior to the {@code target}, if at least the body is not empty/blank.
     * @param target
     * @param tooltipDecorationModel
     */
    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable TooltipDecorationModel tooltipDecorationModel) {

        if(target==null
                || tooltipDecorationModel==null
                || tooltipDecorationModel.isEmpty()) {
            return target; // no body so don't render tooltip
        }

        if(target instanceof ActionLink) {
            var actionLink = (ActionLink)target;
            if(!actionLink.getActionModel().hasParameters()) {
                //XXX[CAUSEWAY-3051] adding a tooltip to an ActionLink will break any ConfirmationBehavior,
                //that's also applied to the ActionLink.
                throw _Exceptions.illegalArgument(
                        "Adding a tooltip to an ActionLink will break any ConfirmationBehavior, "
                        + "that's also applied to same ActionLink!");
            }
        }

        var placementDirection = tooltipDecorationModel.placementDirection();

        final IModel<String> bodyModel = Model.of(tooltipDecorationModel.body());

        var tooltipBehavior = tooltipDecorationModel
                .title()
                .map(title->Model.of(title))
                .map(titleModel->createTooltipBehavior(placementDirection, titleModel, bodyModel))
                .orElseGet(()->createTooltipBehavior(placementDirection, bodyModel));
        target.add(tooltipBehavior);

        Wkt.cssAppend(target, "wkt-component-with-tooltip");
        return target;
    }

    public void clearTooltip(final @Nullable Component target) {
        if(target==null) {
            return;
        }
        target.getBehaviors(TooltipBehavior.class)
        .forEach(target::remove);
    }

    // -- SHORTCUTS

    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable String body) {
        return addTooltip(UiPlacementDirection.BOTTOM, target, body);
    }

    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable String title,
            final @Nullable String body) {
        return addTooltip(UiPlacementDirection.BOTTOM, target, title, body);
    }

    public <T extends Component> T addTooltip(
            final @NonNull UiPlacementDirection uiPlacementDirection,
            final @Nullable T target,
            final @Nullable String body) {
        return addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipDecorationModel.ofBody(uiPlacementDirection, preprocess(body)));
    }

    public <T extends Component> T addTooltip(
            final @NonNull UiPlacementDirection uiPlacementDirection,
            final @Nullable T target,
            final @Nullable String title,
            final @Nullable String body) {
        return addTooltip(target, TooltipDecorationModel.ofTitleAndBody(uiPlacementDirection, title, preprocess(body)));
    }

    // -- HELPER

    private TooltipBehavior createTooltipBehavior(
            final @NonNull UiPlacementDirection uiPlacementDirection,
            final @NonNull IModel<String> titleLabel,
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(titleLabel, bodyLabel, getTooltipConfig(uiPlacementDirection));
    }

    private TooltipBehavior createTooltipBehavior(
            final @NonNull UiPlacementDirection uiPlacementDirection,
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(Model.of(), bodyLabel, getTooltipConfig(uiPlacementDirection));
    }

    private PopoverBehavior createPopoverBehavior(
            final IModel<String> titleLabel,
            final IModel<String> bodyLabel,
            final PopoverConfig config) {

        return new PopoverBehavior(titleLabel, bodyLabel, config) {
            private static final long serialVersionUID = 1L;

            @Override
            protected CharSequence createInitializerScript(final Component component, final Config config) {
                // bootstrap.Popover(...) will fail when the popover trigger element is not found
                // so we wrap the call within a document search, that will only process elements,
                // that actually exist within the DOM
                var markupId = WktComponents.getMarkupId(component);
                return String.format("document.querySelectorAll('#%s').forEach((elem)=>{"
                        + "new bootstrap.Popover(elem, %s);"
                        + "})",
                        markupId,
                        config.toJsonString());
            }
        };
    }

    /**
     * @param uiPlacementDirection top, right, bottom or left
     * @param htmlEnabled - whether to allow HTML for tooltip content
     */
    private PopoverConfig getTooltipConfig(
            final UiPlacementDirection uiPlacementDirection) {
        switch(uiPlacementDirection) {
        case TOP:
            return createPopoverConfigDefault()
                .withPlacement(Placement.top);
        case RIGHT:
            return createPopoverConfigDefault()
                .withPlacement(Placement.right);
        case BOTTOM:
            return createPopoverConfigDefault()
                .withPlacement(Placement.bottom);
        case LEFT:
            return createPopoverConfigDefault()
                .withPlacement(Placement.left);
        default:
            throw _Exceptions.unmatchedCase(uiPlacementDirection);
        }
    }

    private PopoverConfig createPopoverConfigDefault() {
        final TextMode textMode = getTooltipTextMode();
        return new ExtendedPopoverConfig()
                .withBoundary(PopoverBoundary.viewport)
                .withTrigger(OpenTrigger.hover)
                .withDelay(Duration.ZERO)
                .withAnimation(true)
                .withHtml(textMode.isHtml())
                .withSanitizer(!textMode.isHtml());
    }

    /**
     * Lookup in config, else returns default.
     */
    private static TextMode getTooltipTextMode() {
        var textMode = MetaModelContext.instance()
                .map(MetaModelContext::getConfiguration)
                .map(cfg->cfg.getViewer().getWicket().getTooltipTextMode())
                .orElseGet(TextMode::defaults);
        return textMode;
    }

    private static String preprocess(final @Nullable String string) {
        return _Strings.nonEmpty(string)
            .map(s->getTooltipTextMode().isHtml()
                    ? replaceNewlineWithHtmlBR(s)
                    : s)
            .orElse(string);
    }
    private static String replaceNewlineWithHtmlBR(final @NonNull String s) {
        return s.replace("\r", "")
                .replace("\n", "<br/>");
    }

}
