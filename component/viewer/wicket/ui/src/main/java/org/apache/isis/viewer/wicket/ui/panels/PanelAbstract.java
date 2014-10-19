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

import com.google.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.PersistenceSessionProvider;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Convenience adapter for {@link Panel}s built up using {@link ComponentType}s.
 */
// TODO mgrigorov: extend GenericPanel and make T the type of the model object, not the model
public abstract class PanelAbstract<T extends IModel<?>> extends Panel implements IHeaderContributor, PersistenceSessionProvider, AuthenticationSessionProvider {

    private static final long serialVersionUID = 1L;

    private ComponentType componentType;

    public PanelAbstract(final ComponentType componentType) {
        this(componentType, null);
    }

    public PanelAbstract(final String id) {
        this(id, null);
    }

    public PanelAbstract(final ComponentType componentType, final T model) {
        this(componentType.getWicketId(), model);
    }

    public PanelAbstract(final String id, final T model) {
        super(id, model);
        this.componentType = ComponentType.lookup(id);
    }


    /**
     * Will be null if created using {@link #PanelAbstract(String, IModel)}.
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    @SuppressWarnings("unchecked")
    public T getModel() {
        return (T) getDefaultModel();
    }

    /**
     * For subclasses
     * 
     * @return
     */
    protected Component addOrReplace(final ComponentType componentType, final IModel<?> model) {
        return getComponentFactoryRegistry().addOrReplaceComponent(this, componentType, model);
    }

    /**
     * For subclasses
     */
    protected void permanentlyHide(final ComponentType... componentIds) {
        Components.permanentlyHide(this, componentIds);
    }

    /**
     * For subclasses
     */
    public void permanentlyHide(final String... ids) {
        Components.permanentlyHide(this, ids);
    }

    // ///////////////////////////////////////////////////////////////////
    // Header Contributors
    // ///////////////////////////////////////////////////////////////////

    /**
     * Renders the corresponding CSS for this panel.
     * 
     * <p>
     * For most subclasses of {@link PanelAbstract} - specifically those that have their own {@link ComponentFactory}, 
     * it is additionally the responsibility (via {@link ComponentFactory#getCssResourceReference()}) of the factory
     * to declare the {@link CssResourceReference}(s) to be included.  These are then all bundled up into
     * a single unit, as part of the application bootstrapping (<tt>IsisWicketApplication#init</tt>).
     * 
     * <p>
     * This is done because some browsers (we're looking at you, IE!) have a limit of only 31 CSS files.
     * 
     * <p>
     * For subclasses that do not have a {@link ComponentFactory}, their CSS will simply be referenced standalone.
     */
    @Override
    public void renderHead(final IHeaderResponse response) {
//        PanelUtil.renderHead(response, this.getClass());
    }

    
    // ///////////////////////////////////////////////////////////////////
    // Hint support
    // ///////////////////////////////////////////////////////////////////

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this);
    }


    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    /**
     * The underlying {@link AuthenticationSession Isis session} wrapped in the
     * {@link #getWebSession() Wicket session}.
     * 
     * @return
     */
    @Override
    public AuthenticationSession getAuthenticationSession() {
        final AuthenticationSessionProvider asa = (AuthenticationSessionProvider) Session.get();
        return asa.getAuthenticationSession();
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext
    // ///////////////////////////////////////////////////////////////////

    public IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    @Override
    public Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    public AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected List<ObjectAdapter> getServiceAdapters() {
        return IsisContext.getPersistenceSession().getServices();
    }

    // /////////////////////////////////////////////////
    // Dependency Injection
    // /////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
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
