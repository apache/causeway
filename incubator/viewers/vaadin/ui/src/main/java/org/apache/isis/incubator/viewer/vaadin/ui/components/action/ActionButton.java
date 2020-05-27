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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;

import lombok.val;

public class ActionButton extends Button {

    private static final long serialVersionUID = 1L;

    public static ActionButton forManagedAction(ManagedAction managedAction) {
        // TODO yet not doing anything
        val uiAction = new ActionButton(managedAction.getName());
        
        uiAction.getStyle().set("margin-left", "0.5em");
        uiAction.addThemeVariants(
                ButtonVariant.LUMO_SMALL);
        
        val actionDialog = ActionDialog.forManagedAction(managedAction);
        
        uiAction.addClickListener(e->{
            actionDialog.open();
        });
        
        return uiAction;
    }
    
    protected ActionButton(String name) {
        super(name);
    }



}
