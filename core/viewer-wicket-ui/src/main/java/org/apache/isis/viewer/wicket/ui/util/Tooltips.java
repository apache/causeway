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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

public class Tooltips {

    public static void addTooltip(Component target, Component linkIfAny, String tooltipText) {
        //TODO seems TooltipBehavior does not work on menu actions
        //TooltipBehavior tooltipBehavior = new TooltipBehavior(
        //   Model.of(menuItem.getDisabledReason()), createTooltipConfig() );
        //listItem.add(tooltipBehavior);
        //--
        
        target.add(new AttributeModifier("title", Model.of(tooltipText)));
//        // XXX ISIS-1615, prevent bootstrap from changing the HTML link's 'title' attribute on client-side;
//        // bootstrap will not touch the 'title' attribute once the HTML link has a 'data-original-title' attribute
        if(linkIfAny!=null) {
            linkIfAny.add(new AttributeModifier("data-original-title", ""));
        }
//        //--
    }

    
//    private static TooltipConfig createTooltipConfig() {
//        return new TooltipConfig()
//                .withTrigger(OpenTrigger.hover)
//                .withPlacement(Placement.bottom);
//        
//    }
    

}
