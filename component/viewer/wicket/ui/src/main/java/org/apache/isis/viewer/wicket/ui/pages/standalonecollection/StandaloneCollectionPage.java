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

package org.apache.isis.viewer.wicket.ui.pages.standalonecollection;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class StandaloneCollectionPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    /**
     * For use with {@link Component#setResponsePage(org.apache.wicket.Page)}
     */
    public StandaloneCollectionPage(final EntityCollectionModel model) {
        super(new PageParameters(), ApplicationActions.INCLUDE, getActionName(model), ComponentType.STANDALONE_COLLECTION);
        addChildComponents(model);

        addBookmarkedPages();
    }

    private static String getActionName(final EntityCollectionModel model) {
        ActionModel actionModel = model.getActionModelHint();
        if(actionModel != null) {
            return actionModel.getActionMemento().getAction().getName();
        }
        return "Results"; // fallback, probably not required because hint should always exist on the model. 
    }

    // half-completed (copied from ActionPromptPage), but I don't think this is necessary because we are always going
    // to do a redirect to this page.  If you come across this commented-out code and the app is running fine, you can probably delete it.
    
//    public StandaloneCollectionPage(final PageParameters pageParameters) {
//        this(pageParameters, buildModel(pageParameters));
//    }
//    
//    public StandaloneCollectionPage(final PageParameters pageParameters, final EntityCollectionModel model) {
//        super(pageParameters, ApplicationActions.INCLUDE, getActionName(model).getName(), ComponentType.STANDALONE_COLLECTION);
//        addChildComponents(model);
//        
//        // no need to bookmark because the ActionPanel will have done so for us
//        addBookmarkedPages();
//    }
//    
//    private static EntityCollectionModel buildModel(final PageParameters pageParameters) {
//        return EntityCollectionModel.createForPersistent(pageParameters);
//    }
}
