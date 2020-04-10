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

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.incubator.viewer.vaadin.model.entity.ObjectVaa;
import org.apache.isis.viewer.common.model.actionlink.ActionLinkFactory;
import org.apache.isis.viewer.common.model.actionlink.ActionLinkUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MenuActionLinkFactoryVaa implements ActionLinkFactory<Component> {

    /** model of the service that holds the menu item's action */
    private final ObjectVaa serviceModel; 
    
    @Override
    public ActionLinkUiModel<Component> newActionLink(final ObjectAction objectAction) {

        val objectAdapter = serviceModel.getManagedObject();
        
        val model = ActionLinkUiModel.of(
                Component.class,
                objectAdapter, 
                objectAction);
        
        addUiComponentTo(model);
        
        return model;
    }
    
    private void addUiComponentTo(
            final ActionLinkUiModel<Component> model) {
        
        val uiComponent = new HorizontalLayout();
        
        val faIcon = new Span();
        
        Optional.ofNullable(model.getCssClassFa())
        .ifPresent(cssClassFa->{
            _Strings.splitThenStreamTrimmed(cssClassFa, " ")
            .forEach(faIcon::addClassName);
            //faIcon.addClassNames("fa", cssClassFa, "fa-fw");    
        });
        
        uiComponent.add(faIcon, new Label(model.getLabel()));
        
        model.setUiComponent(uiComponent);
        
        
    }
    
}