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
package org.apache.isis.incubator.viewer.vaadin.model.action;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.entity.ObjectVaa;
import org.apache.isis.viewer.common.model.action.ActionUiModel;
import org.apache.isis.viewer.common.model.action.ActionUiModelFactory;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeDecorator;
import org.apache.isis.viewer.common.model.decorator.fa.FontAwesomeUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ActionFactoryVaa implements ActionUiModelFactory<Component> {

    private static class IconDecorator implements FontAwesomeDecorator<Component, Label> {

        @Override
        public Component decorate(
                final Label uiComponent,
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
            
            return decoratedUiComponent;
            
        }
        
    }
    
    private final IconDecorator iconDecorator = new IconDecorator();
    
    
    @Override
    public ActionUiModel<Component> newAction(
            IsisWebAppCommonContext commonContext, 
            String named,
            ManagedObject actionHolder, 
            ObjectAction objectAction) {
        
        val model = new ActionVaa(
                this::createUiComponent,
                named,
                new ObjectVaa(commonContext, actionHolder), 
                objectAction);
        
        return model;
    }
    
    // -- HELPER
    
    private Component createUiComponent(
            final ActionUiModel<Component> model) {
        
        val actionMeta = model.getActionUiMetaModel();
        val uiLabel = new Label(actionMeta.getLabel());
        
        return iconDecorator.decorate(uiLabel, actionMeta.getFontAwesomeUiModel());
                
    }


    
}