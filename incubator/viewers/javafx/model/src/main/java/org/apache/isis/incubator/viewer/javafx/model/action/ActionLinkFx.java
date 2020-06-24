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
package org.apache.isis.incubator.viewer.javafx.model.action;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.HasUiMenuItem;
import org.apache.isis.viewer.common.model.action.ActionLinkUiComponentFactory;
import org.apache.isis.viewer.common.model.action.ActionLinkUiModel;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.common.model.object.SimpleObjectUiModel;

import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;

public class ActionLinkFx 
extends ActionLinkUiModel<Node> 
implements HasUiMenuItem<Menu>{

    public static ActionLinkFx of(
            final String named,
            final ManagedAction managedAction) {
        
        val actionOwnerModel = new SimpleObjectUiModel(managedAction.getOwner());
        return new ActionLinkFx(ActionLinkFx::createUiComponent, named, actionOwnerModel, managedAction.getAction());
    }
    
    protected ActionLinkFx(
            final ActionLinkUiComponentFactory<Node> uiComponentFactory,
            final String named,
            final ObjectUiModel actionHolder,
            final ObjectAction objectAction) {
        
        super(uiComponentFactory, named, actionHolder, objectAction);
    }


    private static Node createUiComponent(final ActionUiMetaModel actionMeta) {
        
        val uiLabel = new Label(actionMeta.getLabel());
        
        return uiLabel;
        //return Decorators.getIcon().decorate(uiLabel, actionMeta.getFontAwesomeUiModel());
                
    }
    
    @Override
    public Menu getUiMenuItem() {
        return new Menu(super.getLabel());
    }
    
}
