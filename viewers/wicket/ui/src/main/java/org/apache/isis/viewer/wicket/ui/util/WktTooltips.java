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
package org.apache.isis.viewer.wicket.ui.util;

import java.time.Duration;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.util.ExtendedPopoverConfig.PopoverBoundary;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
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
     * @param tooltipUiModel
     */
    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable TooltipUiModel tooltipUiModel) {

        if(target==null
                || tooltipUiModel==null
                || tooltipUiModel.isEmpty()) {
            return target; // no body so don't render tooltip
        }

        final IModel<String> bodyModel = Model.of(tooltipUiModel.getBody());

        val tooltipBehavior = tooltipUiModel
                .getTitle()
                .map(title->Model.of(title))
                .map(titleModel->createTooltipBehavior(titleModel, bodyModel))
                .orElseGet(()->createTooltipBehavior(bodyModel));
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
        return addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.ofBody(body));
    }

    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable String title,
            final @Nullable String body) {
        return addTooltip(target, TooltipUiModel.ofTitleAndBody(title, body));
    }

    // -- HELPER

    private TooltipBehavior createTooltipBehavior(
            final @NonNull IModel<String> titleLabel,
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(titleLabel, bodyLabel, getTooltipConfigBottom());
    }

    private TooltipBehavior createTooltipBehavior(
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(Model.of(), bodyLabel, getTooltipConfigBottom());
    }

    private PopoverBehavior createPopoverBehavior(
            final IModel<String> titleLabel,
            final IModel<String> bodyLabel,
            final PopoverConfig config) {

        return new PopoverBehavior(titleLabel, bodyLabel, getTooltipConfigBottom()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(final Component component, final ComponentTag tag) {
                super.onComponentTag(component, tag);
                final Map<String, String> map =
                        _Casts.uncheckedCast(_Json.readJson(Map.class, config.toJsonString()).getValue().get());
                map.forEach((k, v)->{
                    tag.put("data-bs-conf-" + k, ""+v);
                });
                //typical... {"boundary":"viewport","trigger":"hover","placement":"bottom"}
            }

            @Override
            protected CharSequence createInitializerScript(final Component component, final Config config) {
                // batch create these in
                // isis-viewer-wicket-ui/src/main/java/org/apache/isis/viewer/wicket/ui/pages/jquery.isis.wicket.viewer.js
                // val markupId = Strings2.getMarkupId(Args.notNull(component, "component"));
                return "";
            }
        };
    }

    @Getter(lazy=true)
    private final PopoverConfig tooltipConfigTop =
            createPopoverConfigDefault()
            .withPlacement(Placement.top);

    @Getter(lazy=true)
    private final PopoverConfig tooltipConfigBottom =
            createPopoverConfigDefault()
                .withPlacement(Placement.bottom);

    private PopoverConfig createPopoverConfigDefault() {
        return new ExtendedPopoverConfig()
                .withBoundary(PopoverBoundary.viewport)
                .withTrigger(OpenTrigger.hover)
                .withDelay(Duration.ZERO)
                .withAnimation(true);
    }
}
