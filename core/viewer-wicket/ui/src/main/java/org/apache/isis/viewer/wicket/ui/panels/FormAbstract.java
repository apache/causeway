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

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.Getter;

public abstract class FormAbstract<T> extends Form<T>
implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor {

    private static final long serialVersionUID = 1L;

    public FormAbstract(final String id) {
        super(id);
        this.commonContext = IsisWebAppCommonContext.of(metaModelContext);
    }

    public FormAbstract(final String id, final IModel<T> model) {
        super(id, model);
        this.commonContext = IsisWebAppCommonContext.of(metaModelContext);
    }

    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    @Override
    public ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
    }

    @Override
    public PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) getApplication()).getPageClassRegistry();
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    protected  PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }

    protected  AuthenticationSession getAuthenticationSession() {
        return commonContext.getAuthenticationSession();
    }

    @Inject @Getter protected transient SpecificationLoader specificationLoader;
    @Inject @Getter protected transient ServiceRegistry serviceRegistry;
    @Inject @Getter protected transient TranslationService translationService;
    @Inject private transient MetaModelContext metaModelContext;
    @Getter protected final transient IsisWebAppCommonContext commonContext;

}
