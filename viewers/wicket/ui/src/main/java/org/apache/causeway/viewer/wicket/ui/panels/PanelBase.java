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
package org.apache.causeway.viewer.wicket.ui.panels;

import java.util.function.Supplier;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.i18n.LanguageProvider;
import org.apache.causeway.applib.services.userreg.EmailNotificationService;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiModel;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiService;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.ImageResourceCache;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.tree.themes.TreeThemeProvider;
import org.apache.causeway.viewer.wicket.ui.components.tree.themes.TreeThemeProviderDefault;
import org.apache.causeway.viewer.wicket.ui.pages.EmailVerificationUrlService;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.PageNavigationService;

/**
 * Provides the <em>common context</em> for all implementing sub-classes.
 * @since 2.0
 */
public class PanelBase<T>
extends GenericPanel<T>
implements HasCommonContext {

    private static final long serialVersionUID = 1L;

    protected PanelBase(final String id) {
        this(id, null);
    }

    public PanelBase(final String id, @Nullable final IModel<T> model) {
        super(id, model);
    }

    private transient MetaModelContext mmc;
    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

    private transient PageClassRegistry pageClassRegistry;
    public PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry = computeIfAbsent(PageClassRegistry.class, pageClassRegistry);
    }

    private transient ImageResourceCache imageCache;
    public ImageResourceCache getImageResourceCache() {
        return imageCache = computeIfAbsent(ImageResourceCache.class, imageCache);
    }

    private transient LanguageProvider localeProvider;
    public LanguageProvider getLanguageProvider() {
        return localeProvider = computeIfAbsent(LanguageProvider.class, localeProvider);
    }

    private transient TreeThemeProvider treeThemeProvider;
    protected TreeThemeProvider getTreeThemeProvider() {
        return treeThemeProvider = computeIfAbsentOrFallback(TreeThemeProvider.class, treeThemeProvider, TreeThemeProviderDefault::new);
    }

    private transient EmailNotificationService emailNotificationService;
    protected EmailNotificationService getEmailNotificationService() {
        return emailNotificationService = computeIfAbsent(EmailNotificationService.class, emailNotificationService);
    }

    private transient EmailVerificationUrlService emailVerificationUrlService;
    protected EmailVerificationUrlService getEmailVerificationUrlService() {
        return emailVerificationUrlService = computeIfAbsent(EmailVerificationUrlService.class, emailVerificationUrlService);
    }

    private transient PageNavigationService pageNavigationService;
    protected PageNavigationService getPageNavigationService() {
        return pageNavigationService = computeIfAbsent(PageNavigationService.class, pageNavigationService);
    }

    private transient HeaderUiService headerUiService;
    protected HeaderUiModel getHeaderModel() {
        headerUiService = computeIfAbsent(HeaderUiService.class, headerUiService);
        return headerUiService.getHeader();
    }

    // Hint support

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this);
    }

    // other Dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((HasComponentFactoryRegistry) getApplication()).getComponentFactoryRegistry();
    }

    // -- HELPER

    private <X> X computeIfAbsent(final Class<X> type, final X existingIfAny) {
        return existingIfAny!=null
                ? existingIfAny
                        : lookupServiceElseFail(type);
    }

    private <X> X computeIfAbsentOrFallback(final Class<X> type, final X existingIfAny, final Supplier<X> fallback) {
        return existingIfAny!=null
                ? existingIfAny
                        : lookupServiceElseFallback(type, fallback);
    }

}
