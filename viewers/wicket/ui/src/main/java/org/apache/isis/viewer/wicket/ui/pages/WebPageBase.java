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

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Viewer.Wicket;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.viewer.wicket.model.util.WktContext;

/**
 * Provides all the system dependencies for sub-classes.
 * @since 2.0
 */
public abstract class WebPageBase
extends WebPage
implements HasMetaModelContext {

    private static final long serialVersionUID = 1L;

    private transient WebAppContextPath webAppContextPath;
    private transient PageClassRegistry pageClassRegistry;
    private transient MetaModelContext commonContext;
    private transient InteractionService interactionService;

    protected WebPageBase(final PageParameters parameters) {
        super(parameters);
    }

    protected WebPageBase(final IModel<?> model) {
        super(model);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderFavicon(response);
    }

    // -- FAVICON SUPPORT

    protected void renderFavicon(final IHeaderResponse response) {
        getWicketViewerSettings().getApplication().getFaviconUrl()
        .filter(_Strings::isNotEmpty)
        .map(getWebAppContextPath()::prependContextPathIfLocal)
        .ifPresent(faviconUrl->{
            response.render(MetaDataHeaderItem.forLinkTag("icon", faviconUrl));
        });
    }

    // -- DEPENDENCIES

    @Override
    public MetaModelContext getMetaModelContext() {
        return commonContext = WktContext.computeIfAbsent(commonContext);
    }

    @Override
    public IsisConfiguration getConfiguration() {
        return getMetaModelContext().getConfiguration();
    }

    @Override
    public WebAppContextPath getWebAppContextPath() {
        return webAppContextPath = computeIfAbsent(WebAppContextPath.class, webAppContextPath);
    }

    public PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry = computeIfAbsent(PageClassRegistry.class, pageClassRegistry);
    }

    public InteractionService getInteractionService() {
        return interactionService = computeIfAbsent(InteractionService.class, interactionService);
    }

    @Override
    public IsisSystemEnvironment getSystemEnvironment() {
        return getMetaModelContext().getSystemEnvironment();
    }

    protected Wicket getWicketViewerSettings() {
        return getConfiguration().getViewer().getWicket();
    }

    // -- HELPER

    private <X> X computeIfAbsent(final Class<X> type, final X existingIfAny) {
        return existingIfAny!=null
                ? existingIfAny
                : getMetaModelContext().lookupServiceElseFail(type);
    }

}
