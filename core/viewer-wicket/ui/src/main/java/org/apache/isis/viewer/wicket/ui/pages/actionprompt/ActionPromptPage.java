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

package org.apache.isis.viewer.wicket.ui.pages.actionprompt;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ActionPromptPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    /**
     * For use with {@link Component#setResponsePage(org.apache.wicket.Page)}
     */
    public ActionPromptPage(final ActionModel model) {
        super(new PageParameters(), model.getActionMemento().getAction().getName(), ComponentType.ACTION_PROMPT);
        addChildComponents(themeDiv, model);

        if(model.isBookmarkable()) {
            bookmarkPage(model);
        }
        addBookmarkedPages();
    }

    public ActionPromptPage(final PageParameters pageParameters) {
        this(pageParameters, buildModel(pageParameters));
    }
    
    public ActionPromptPage(final PageParameters pageParameters, final ActionModel model) {
        super(pageParameters, model.getActionMemento().getAction().getName(), ComponentType.ACTION_PROMPT);
        addChildComponents(themeDiv, model);
        
        // no need to bookmark because the ActionPanel will have done so for us
        addBookmarkedPages();
    }
    
    private static ActionModel buildModel(final PageParameters pageParameters) {
        return ActionModel.createForPersistent(pageParameters);
    }
}
