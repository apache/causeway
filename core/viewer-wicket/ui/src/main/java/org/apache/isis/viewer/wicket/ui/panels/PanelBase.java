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

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.Getter;

/**
 * Provides the <em>common context</em> for all implementing sub-classes.
 * @since 2.0
 */
public class PanelBase<T> extends GenericPanel<T> implements IsisWebAppCommonContext.Delegating {

    private static final long serialVersionUID = 1L;

    @Inject @Getter protected transient WicketViewerSettings settings;
    @Inject protected transient WebAppConfigBean webAppConfigBean;
    @Inject @Getter protected transient PageClassRegistry pageClassRegistry;
    @Inject @Getter protected transient ImageResourceCache imageCache;
    
    @Inject private transient MetaModelContext metaModelContext;
    @Getter protected final transient IsisWebAppCommonContext commonContext;
    
    @Getter protected final transient IsisSessionFactory isisSessionFactory;
    @Getter protected final transient TranslationService translationService;
    @Getter protected final transient LocaleProvider localeProvider;
    
    protected PanelBase(String id) {
        this(id, null);
    }
    
    public PanelBase(String id, @Nullable IModel<T> model) {
        super(id, model);
        this.commonContext = IsisWebAppCommonContext.of(metaModelContext);
        this.isisSessionFactory = commonContext.lookupServiceElseFail(IsisSessionFactory.class);
        this.translationService = commonContext.lookupServiceElseFail(TranslationService.class);
        this.localeProvider = commonContext.lookupServiceElseFail(LocaleProvider.class);
    }
    
    protected MessageBroker getMessageBroker() {
        return commonContext.getAuthenticationSession().getMessageBroker();
    }

    // Hint support

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this);
    }

    // other Dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
    }


}
