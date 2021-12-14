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
package org.apache.isis.viewer.wicket.ui.panels;

import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.LanguageProvider;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.userreg.EmailNotificationService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.common.model.header.HeaderUiModel;
import org.apache.isis.viewer.common.model.header.HeaderUiModelProvider;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.tree.themes.TreeThemeProvider;
import org.apache.isis.viewer.wicket.ui.components.tree.themes.TreeThemeProviderDefault;
import org.apache.isis.viewer.wicket.ui.pages.EmailVerificationUrlService;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;

/**
 * Provides the <em>common context</em> for all implementing sub-classes.
 * @since 2.0
 */
public class PanelBase<T>
extends GenericPanel<T>
implements HasCommonContext {

    private static final long serialVersionUID = 1L;

    private transient WicketViewerSettings wicketViewerSettings;
    private transient WebAppContextPath webAppContextPath;
    private transient IsisConfiguration isisConfiguration;
    private transient PageClassRegistry pageClassRegistry;
    private transient ImageResourceCache imageCache;
    private transient MetaModelContext metaModelContext;
    private transient IsisAppCommonContext commonContext;
    private transient InteractionService interactionService;
    private transient TranslationService translationService;
    private transient LanguageProvider localeProvider;
    private transient TreeThemeProvider treeThemeProvider;
    private transient EmailNotificationService emailNotificationService;
    private transient EmailVerificationUrlService emailVerificationUrlService;
    private transient PageNavigationService pageNavigationService;
    private transient HeaderUiModelProvider headerUiModelProvider;

    protected PanelBase(String id) {
        this(id, null);
    }

    public PanelBase(String id, @Nullable IModel<T> model) {
        super(id, model);
    }

    @Override
    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    public WicketViewerSettings getWicketViewerSettings() {
        return wicketViewerSettings = computeIfAbsent(WicketViewerSettings.class, wicketViewerSettings);
    }

    public WebAppContextPath getWebAppContextPath() {
        return webAppContextPath = computeIfAbsent(WebAppContextPath.class, webAppContextPath);
    }

    public IsisConfiguration getIsisConfiguration() {
        return isisConfiguration = computeIfAbsent(IsisConfiguration.class, isisConfiguration);
    }

    public PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry = computeIfAbsent(PageClassRegistry.class, pageClassRegistry);
    }

    public ImageResourceCache getImageResourceCache() {
        return imageCache = computeIfAbsent(ImageResourceCache.class, imageCache);
    }

    public MetaModelContext getMetaModelContext() {
        return metaModelContext = computeIfAbsent(MetaModelContext.class, metaModelContext);
    }

    public InteractionService getInteractionService() {
        return interactionService = computeIfAbsent(InteractionService.class, interactionService);
    }

    public TranslationService getTranslationService() {
        return translationService = computeIfAbsent(TranslationService.class, translationService);
    }

    public LanguageProvider getLanguageProvider() {
        return localeProvider = computeIfAbsent(LanguageProvider.class, localeProvider);
    }

    protected TreeThemeProvider getTreeThemeProvider() {
        return treeThemeProvider = computeIfAbsentOrFallback(TreeThemeProvider.class, treeThemeProvider, TreeThemeProviderDefault::new);
    }

    protected EmailNotificationService getEmailNotificationService() {
        return emailNotificationService = computeIfAbsent(EmailNotificationService.class, emailNotificationService);
    }

    protected EmailVerificationUrlService getEmailVerificationUrlService() {
        return emailVerificationUrlService = computeIfAbsent(EmailVerificationUrlService.class, emailVerificationUrlService);
    }

    protected PageNavigationService getPageNavigationService() {
        return pageNavigationService = computeIfAbsent(PageNavigationService.class, pageNavigationService);
    }

    protected MessageBroker getMessageBroker() {
        return getCommonContext().getMessageBroker()
        .orElseThrow(()->_Exceptions.illegalState(
                "no MessageBroker available on current session"));
    }

    protected HeaderUiModel getHeaderModel() {
        headerUiModelProvider = computeIfAbsent(HeaderUiModelProvider.class, headerUiModelProvider);
        return headerUiModelProvider.getHeader();
    }

    // Hint support

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this);
    }

    // other Dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
    }

    // -- HELPER

    private <X> X computeIfAbsent(Class<X> type, X existingIfAny) {
        return existingIfAny!=null
                ? existingIfAny
                        : getCommonContext().lookupServiceElseFail(type);
    }

    private <X> X computeIfAbsentOrFallback(Class<X> type, X existingIfAny, Supplier<X> fallback) {
        return existingIfAny!=null
                ? existingIfAny
                        : getCommonContext().lookupServiceElseFallback(type, fallback);
    }

}
