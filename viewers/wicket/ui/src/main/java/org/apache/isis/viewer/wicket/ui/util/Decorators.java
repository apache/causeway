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

import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeUiModel;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;

import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * 
 */
@UtilityClass
public class Decorators {

    @Getter(lazy = true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) private final static IconDecorator iconDecorator = new IconDecorator();
    @Getter(lazy = true) private final static MissingIconDecorator missingIconDecorator = new MissingIconDecorator();
    
    // -- DECORATOR CLASSES
    
    public final static class Tooltip implements TooltipDecorator<Component> {

        @Override
        public void addTooltip(Component uiComponent, TooltipUiModel tooltipUiModel) {
            Tooltips.addTooltip(uiComponent, tooltipUiModel);
        }
        
    }
    
    //TODO make available for reuse
    public final static class IconDecorator implements FontAwesomeDecorator<Component, Component> {

        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(fontAwesome.isPresent()) {
                uiComponent.add(new CssClassFaBehavior(fontAwesome.get()));
            } 
            return uiComponent;
        }
        
    }

    //TODO make available for reuse
    public final static class MissingIconDecorator implements FontAwesomeDecorator<Component, Component> {

        @Override
        public Component decorate(Component uiComponent, Optional<FontAwesomeUiModel> fontAwesome) {
            if(!fontAwesome.isPresent()) {
                uiComponent.add(new CssClassAppender("menuLinkSpacer"));
            }
            return uiComponent;
        }
        
    }
    
}
