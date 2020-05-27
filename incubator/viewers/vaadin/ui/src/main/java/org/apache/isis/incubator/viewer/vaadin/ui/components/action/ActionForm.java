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
package org.apache.isis.incubator.viewer.vaadin.ui.components.action;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;

import lombok.NonNull;
import lombok.val;

public class ActionForm extends FormLayout {

    private static final long serialVersionUID = 1L;
    
    private final transient ManagedAction managedAction;
    
    public static ActionForm forManagedAction(
            @NonNull final UiComponentFactoryVaa uiComponentFactory,
            @NonNull final ManagedAction managedAction) {
        
        val actionForm = new ActionForm(uiComponentFactory, managedAction);
        return actionForm;
    }

    protected ActionForm(
            final UiComponentFactoryVaa uiComponentFactory,
            final ManagedAction managedAction) {
        
        this.managedAction = managedAction;
        
        managedAction.getAction().getParameters()
        .forEach(param->{
        
            val paramField = new TextField();
            paramField.setLabel(param.getName());
            paramField.setPlaceholder("under construction");
            
            super.add(paramField);
            
            
//            val uiParameter = uiComponentFactory
//                    .componentFor(UiComponentFactory.Request.of(Where.ANYWHERE, param));
            
        });
        
        
        
        
    }
    
}
