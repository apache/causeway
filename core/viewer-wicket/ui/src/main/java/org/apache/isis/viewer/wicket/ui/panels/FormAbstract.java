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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
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
    // process() override
    // ///////////////////////////////////////////////////////////////////

    private String preValidationErrorIfAny;
    /**
     * Temporarily made available during {@link #process(IFormSubmitter)},
     * for the benefit of any form validation.
     */
    protected String getPreValidationErrorIfAny() {
        return preValidationErrorIfAny;
    }
    
    @Override
    public void process(final IFormSubmitter submittingComponent) {
        try {
            
            if(submittingComponent instanceof IFormSubmitterWithPreValidateHook) {
                IFormSubmitterWithPreValidateHook componentWithPreSubmitHook = (IFormSubmitterWithPreValidateHook) submittingComponent;
                preValidationErrorIfAny = componentWithPreSubmitHook.preValidate();
            }
            
            if(preValidationErrorIfAny != null) {
                // an exception has already occurred, so disable for remainder of thread.
                AdapterManager.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(new Runnable(){
                    @Override
                    public void run() {
                        FormAbstract.super.process(submittingComponent);
                    }
                });
            } else {
                super.process(submittingComponent);
            }

        } finally {
            preValidationErrorIfAny = null;
        }
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

    @Override
    public PersistenceSession getPersistenceSession() {
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
