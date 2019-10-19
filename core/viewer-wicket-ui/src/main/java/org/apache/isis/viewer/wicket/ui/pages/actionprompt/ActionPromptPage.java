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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tracing.Scope2;
import org.apache.isis.core.tracing.ScopeManager2;
import org.apache.isis.core.tracing.TraceScopeManager;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ActionPromptPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    public ActionPromptPage(final ActionModel model) {
        super(new PageParameters(), model.getActionMemento().getAction(model.getSpecificationLoader()).getName(), ComponentType.ACTION_PROMPT);

        TraceScopeManager.get()
                .execInScope("ActionPromptPage#<init>", new ScopeManager2.Executable() {
                    @Override
                    public void exec(Scope2 scope2) {
                        scope2.span().setTag("user", IsisContext.getSessionFactory().getCurrentSession().getAuthenticationSession().getUserName());

                        addChildComponents(themeDiv, model);

                        if(model.isBookmarkable()) {
                            bookmarkPageIfShown(model);
                        }
                        addBookmarkedPages(themeDiv);

                    }
                });

    }

    /**
     * Required for bookmarking of actions.
     */
    public ActionPromptPage(final PageParameters pageParameters) {
        this(pageParameters, IsisContext.getSessionFactory().getSpecificationLoader());
    }

    public ActionPromptPage(final PageParameters pageParameters, final SpecificationLoader specificationLoader) {
        this(pageParameters, buildModel(pageParameters, specificationLoader));
    }

    public ActionPromptPage(final PageParameters pageParameters, final ActionModel model) {
        super(pageParameters, model.getActionMemento().getAction(model.getSpecificationLoader()).getName(), ComponentType.ACTION_PROMPT);
        addChildComponents(themeDiv, model);
        
        // no need to bookmark because the ActionParametersPanel will have done so for us
        addBookmarkedPages(themeDiv);
    }
    
    private static ActionModel buildModel(
            final PageParameters pageParameters,
            final SpecificationLoader specificationLoader) {
        final ActionModel actionModel = ActionModel.createForPersistent(pageParameters, specificationLoader);
        return actionModel;
    }
}
