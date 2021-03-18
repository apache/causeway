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
package org.apache.isis.viewer.wicket.ui.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;

/**
 * Provides all the system dependencies for sub-classes.
 * @since 2.0
 */
public abstract class WebPageBase
extends WebPage 
implements HasCommonContext {

    private static final long serialVersionUID = 1L;
    
    private transient IsisConfiguration isisConfiguration;
    private transient WebAppContextPath webAppContextPath;
    private transient PageClassRegistry pageClassRegistry;
    private transient IsisAppCommonContext commonContext;
    private transient InteractionFactory isisInteractionFactory;
    
    protected WebPageBase(PageParameters parameters) {
        super(parameters);
    }

    protected WebPageBase(final IModel<?> model) {
        super(model);
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        renderFavicon(response);
    }

    // -- FAVICON SUPPORT
    
    protected void renderFavicon(IHeaderResponse response) {
        getIsisConfiguration().getViewer().getWicket().getApplication().getFaviconUrl()
        .filter(_Strings::isNotEmpty)
        .map(getWebAppContextPath()::prependContextPathIfLocal)
        .ifPresent(faviconUrl->{
            response.render(MetaDataHeaderItem.forLinkTag("icon", faviconUrl));    
        });
    }
    
    // -- DEPENDENCIES
    
    @Override
    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }
    
    public IsisConfiguration getIsisConfiguration() {
        return isisConfiguration = computeIfAbsent(IsisConfiguration.class, isisConfiguration);
    }

    public WebAppContextPath getWebAppContextPath() {
        return webAppContextPath = computeIfAbsent(WebAppContextPath.class, webAppContextPath);
    }
    
    public PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry = computeIfAbsent(PageClassRegistry.class, pageClassRegistry);
    }

    public InteractionFactory getIsisInteractionFactory() {
        return isisInteractionFactory = computeIfAbsent(InteractionFactory.class, isisInteractionFactory);
    }
    
    // -- HELPER
    
    private <X> X computeIfAbsent(Class<X> type, X existingIfAny) {
        return existingIfAny!=null
                ? existingIfAny
                : getCommonContext().lookupServiceElseFail(type);
    }
    
}
