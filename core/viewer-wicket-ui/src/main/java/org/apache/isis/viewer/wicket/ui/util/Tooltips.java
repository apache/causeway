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

import static org.apache.isis.commons.internal.functions._Predicates.alwaysTrue;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.commons.internal.base._Strings;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
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
    
    public static void addTooltip(Component target, Model<String> tooltipTextModel) {
        if(tooltipTextModel==null) {
            return;
        }
        final String tooltipText = tooltipTextModel.getObject();
        addTooltip(target, tooltipText);
    }

    public static void addTooltip(Component target, String tooltipText) {
        
        if(_Strings.isNullOrEmpty(tooltipText)) {
            return;
        }
        
        final TooltipBehavior tooltipBehavior = new TooltipBehavior(
                Model.of(tooltipText), createTooltipConfig() );
        
        target.add(new AttributeAppender("class", " isis-component-with-tooltip"));    
        target.add(tooltipBehavior);
    }
    
    public static void clearTooltip(Component target) {
        target.getBehaviors(TooltipBehavior.class).removeIf(alwaysTrue());
    }
    
    // -- HELPER

    private static TooltipConfig createTooltipConfig() {
        return new TooltipConfig()
                .withTrigger(OpenTrigger.hover)
                .withPlacement(Placement.bottom)
                .withAnimation(true);
        

    }

   


}
