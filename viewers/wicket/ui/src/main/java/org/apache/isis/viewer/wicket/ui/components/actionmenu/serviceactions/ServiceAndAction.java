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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

class ServiceAndAction {
    final String actionName;
    final EntityModel serviceEntityModel;
    final ObjectAction objectAction;
    final ServiceActionLinkFactory linkAndLabelFactory;
    final boolean isFirstSection;

    ServiceAndAction(
            final String actionName,
            final EntityModel serviceEntityModel,
            final ObjectAction objectAction,
            final boolean isFirstSection) {
        
        this.actionName = actionName;
        this.serviceEntityModel = serviceEntityModel;
        this.objectAction = objectAction;
        this.linkAndLabelFactory = new ServiceActionLinkFactory(PageAbstract.ID_MENU_LINK, serviceEntityModel);
        this.isFirstSection = isFirstSection; 
    }

    @Override
    public String toString() {
        return actionName + " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }

}
