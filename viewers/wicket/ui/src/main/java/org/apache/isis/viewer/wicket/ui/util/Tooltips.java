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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import lombok.NonNull;
import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.OpenTrigger;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

public class Tooltips {

    /**
     * To include the tooltip-css when a page is rendered.
     * @param response
     */
    public static void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new CssResourceReference(Tooltips.class, "isis-tooltips.css"))); 
    }
    
    public static void addTooltip(
            @NonNull Component target, 
            IModel<String> labelModel,  
            IModel<String> bodyModel) {
        if(bodyModel==null || bodyModel.getObject()==null) {
            return; // no body so don't render
        }
        if(labelModel==null) {
            labelModel = emptyModel();
        }
        val tooltipBehavior = createTooltipBehavior(labelModel, bodyModel);
        target.add(new AttributeAppender("class", " isis-component-with-tooltip"));    
        target.add(tooltipBehavior);
    }

    public static void clearTooltip(Component target) {
        target.getBehaviors(PopoverBehavior.class)
        .forEach(target::remove);
    }
    
    // -- SHORTCUTS
    
    public static void addTooltip(@NonNull Component target, String body) {
        addTooltip(target, emptyModel(), Model.of(body));
    }
    
    public static void addTooltip(@NonNull Component target, IModel<String> bodyModel) {
        addTooltip(target, emptyModel(), bodyModel);
    }

    public static void addTooltip(@NonNull Component target, String label, String body) {
        addTooltip(target, Model.of(label), Model.of(body));
    }

    // -- HELPER
    
    private static IModel<String> emptyModel() {
        return Model.of();
    }

    private static PopoverBehavior createTooltipBehavior(IModel<String> label, IModel<String> body) {
        return new PopoverBehavior(label, body, createTooltipConfig());
    }
    
    private static PopoverConfig createTooltipConfig() {
        return new PopoverConfig()
                .withTrigger(OpenTrigger.hover)
                .withPlacement(Placement.bottom)
                .withAnimation(true);
    }

}
