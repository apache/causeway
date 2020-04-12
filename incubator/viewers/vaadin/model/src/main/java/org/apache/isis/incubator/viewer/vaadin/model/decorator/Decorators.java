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
package org.apache.isis.incubator.viewer.vaadin.model.decorator;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeUiModel;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipDecorator;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;

import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * 
 */
@UtilityClass
@Log4j2
public class Decorators {

    @Getter(lazy = true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) private final static Icon icon = new Icon();
    
    // -- DECORATOR CLASSES
    
    public final static class Tooltip implements TooltipDecorator<Component> {

        @Override
        public void decorate(Component uiComponent, TooltipUiModel tooltipUiModel) {
            log.warn("not implemented yet");
        }
        
    }
    
    public final static class Icon implements FontAwesomeDecorator<Component> {

        @Override
        public Component decorate(
                final Component uiComponent,
                final Optional<FontAwesomeUiModel> fontAwesomeUiModel) {
            
            val decoratedUiComponent = fontAwesomeUiModel
                    .map(fontAwesome->{

                        val faIcon = new Span();
                        
                        _Strings.splitThenStreamTrimmed(fontAwesome.getCssClass(), " ")
                        .forEach(faIcon::addClassName);
                        
                        return CssClassFaPosition.isLeftOrUnspecified(fontAwesome.getPosition())
                                ? new HorizontalLayout(faIcon, uiComponent)
                                : new HorizontalLayout(uiComponent, faIcon);
                                
                    })
                    .orElseGet(()->{
                        
                        // TODO add spacer, to account for missing fa icon?
                        // but then where to add, left or right?
                        
                        return new HorizontalLayout(uiComponent);
                    });
            
            return (HorizontalLayout)decoratedUiComponent;
            
        }
        
    }



    
}
