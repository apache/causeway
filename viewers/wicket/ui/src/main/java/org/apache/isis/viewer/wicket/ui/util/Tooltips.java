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

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.util.ExtendedPopoverConfig.PopoverBoundary;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.OpenTrigger;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@UtilityClass
public class Tooltips {

    /**
     * To include the tooltip-css when a page is rendered.
     * @param response
     */
    public static void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new CssResourceReference(Tooltips.class, "isis-tooltips.css"))); 
    }
    
    /**
     * Adds popover behavior to the {@code target}, if at least the body is not empty/blank.
     * @param target
     * @param tooltipUiModel
     */
    public static void addTooltip(
            @NonNull final Component target,
            @Nullable final TooltipUiModel tooltipUiModel) {
        if(tooltipUiModel==null || _Strings.isEmpty(tooltipUiModel.getBody())) {
            return; // no body so don't render
        }
        
        final IModel<String> labelModel = tooltipUiModel
                .getLabel()
                .map(label->Model.of(label))
                .orElseGet(()->Model.of());  
        final IModel<String> bodyModel = Model.of(tooltipUiModel.getBody());
        
        val tooltipBehavior = createTooltipBehavior(labelModel, bodyModel);
        target.add(new CssClassAppender("isis-component-with-tooltip"));    
        target.add(tooltipBehavior);
    }

    public static void clearTooltip(Component target) {
        target.getBehaviors(PopoverBehavior.class)
        .forEach(target::remove);
    }
    
    // -- SHORTCUTS
    
    //sonar-ignore-on ... fails to interpret _Strings.isEmpty as null guard
    
    public static void addTooltip(@NonNull Component target, @Nullable String body) {
        addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.ofBody(body));
    }

    public static void addTooltip(@NonNull Component target, @Nullable String label, @Nullable String body) {
        addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.of(label, body));
    }
    
    //sonar-ignore-off

    // -- HELPER
    
    private static PopoverBehavior createTooltipBehavior(IModel<String> label, IModel<String> body) {
        return new PopoverBehavior(label, body, createTooltipConfig());
    }
    
    private static PopoverConfig createTooltipConfig() {
        return new ExtendedPopoverConfig()
        		.withBoundary(PopoverBoundary.viewport)
                .withTrigger(OpenTrigger.hover)
                .withPlacement(Placement.bottom)
                .withAnimation(true);
    }

}
