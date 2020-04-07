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
package org.apache.isis.incubator.viewer.vaadin.ui.components.actionlink;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.incubator.viewer.vaadin.model.entity.ObjectVaa;
import org.apache.isis.viewer.common.model.link.ActionLinkFactory;
import org.apache.isis.viewer.common.model.link.LinkAndLabelUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MenuActionLinkFactory implements ActionLinkFactory<Component> {

    /** model of the service that holds the menu item's action */
    private final ObjectVaa serviceModel; 
    
    @Override
    public LinkAndLabelUiModel<Component> newLink(final ObjectAction objectAction) {

        val objectAdapter = serviceModel.getManagedObject();
        val linkComponent = new Label(objectAction.getName());
        val whetherReturnsBlobOrClob = ObjectAction.Util.returnsBlobOrClob(objectAction);
        
        //linkComponent.addClassName(className);
        
        return LinkAndLabelUiModel.newLinkAndLabel(linkComponent, objectAdapter, objectAction, whetherReturnsBlobOrClob);
        
    }
}