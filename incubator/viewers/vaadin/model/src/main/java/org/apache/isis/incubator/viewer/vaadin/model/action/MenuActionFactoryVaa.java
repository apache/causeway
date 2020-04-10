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

import com.vaadin.flow.component.Component;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.entity.ObjectVaa;
import org.apache.isis.viewer.common.model.action.ActionFactory;

import lombok.val;

public class MenuActionFactoryVaa implements ActionFactory<Component> {

    @Override
    public MenuActionVaa newAction(
            IsisWebAppCommonContext commonContext, 
            String named,
            ManagedObject actionHolder,
            ObjectAction objectAction) {
        
        val objectModel = new ObjectVaa(commonContext, actionHolder);
        
        return new MenuActionVaa(
                        new MenuActionLinkFactoryVaa(objectModel),
                        named,
                        objectAction,
                        objectModel);
    }

}
