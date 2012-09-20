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

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.wicket.model.isis.PersistenceSessionProvider;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;

public abstract class FormAbstract<T> extends Form<T> implements IHeaderContributor, ComponentFactoryRegistryAccessor, PageClassRegistryAccessor, AuthenticationSessionProvider, PersistenceSessionProvider {

    private static final long serialVersionUID = 1L;

    public FormAbstract(final String id) {
        super(id);
    }

    public FormAbstract(final String id, final IModel<T> model) {
        super(id, model);
    }

    // ///////////////////////////////////////////////////////////////////
    // IHeaderContributor
    // ///////////////////////////////////////////////////////////////////

    /**
     * Automatically reference any corresponding CSS.
     */
    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderHead(response, this.getClass());
    }

    /**
     * Factored out to allow non-concrete subclasses to additionally render
     * their own CSS if required.
     */
    protected void renderHead(final IHeaderResponse response, final Class<?> cls) {
        final String url = cls.getSimpleName() + ".css";
        //response.renderCSSReference(new PackageResourceReference(cls, url));
        response.render(CssHeaderItem.forReference(new CssResourceReference(cls, url)));
    }


    // ///////////////////////////////////////////////////////////////////
    // process() override
    // ///////////////////////////////////////////////////////////////////

    @Override
    public void process(IFormSubmitter submittingComponent) {
        if(submittingComponent instanceof IFormSubmitterWithPreSubmitHook) {
            IFormSubmitterWithPreSubmitHook componentWithPreSubmitHook = (IFormSubmitterWithPreSubmitHook) submittingComponent;
            componentWithPreSubmitHook.preSubmit();
        }
        super.process(submittingComponent);
    }
    

    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    @Inject
    private ComponentFactoryRegistry componentFactoryRegistry;
    
    @Override
    public ComponentFactoryRegistry getComponentFactoryRegistry() {
        //return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
        return componentFactoryRegistry;
    }

    @Override
    public PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) getApplication()).getPageClassRegistry();
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    public IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    @Override
    public Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    @Override
    public AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected List<ObjectAdapter> getServiceAdapters() {
        return IsisContext.getPersistenceSession().getServices();
    }

    // /////////////////////////////////////////////////
    // *Provider impl.
    // /////////////////////////////////////////////////
    
    @Override
    public void injectInto(final Object candidate) {
        if (AuthenticationSessionProviderAware.class.isAssignableFrom(candidate.getClass())) {
            final AuthenticationSessionProviderAware cast = AuthenticationSessionProviderAware.class.cast(candidate);
            cast.setAuthenticationSessionProvider(this);
        }
    }

}
