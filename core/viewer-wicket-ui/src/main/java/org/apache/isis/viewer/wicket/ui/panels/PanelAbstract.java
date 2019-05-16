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

import java.util.function.Function;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Convenience adapter for {@link Panel}s built up using {@link ComponentType}s.
 */
// TODO mgrigorov: extend GenericPanel and make T the type of the model object, not the model
public abstract class PanelAbstract<T extends IModel<?>> extends Panel {

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
    public AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
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

    /**
     * Helper method that looks up a domain service by type
     *
     * @param serviceClass The class of the domain service to lookup
     * @param <S> The type of the domain service to lookup
     * @return The found domain service
     */
    protected <S> S lookupService(final Class<S> serviceClass) {
        return IsisContext.getServiceRegistry().lookupService(serviceClass).orElse(null);
    }


    protected void addConfirmationDialogIfAreYouSureSemantics(final Component component, final SemanticsOf semanticsOf) {
        PanelUtil.addConfirmationDialogIfAreYouSureSemantics(component, semanticsOf);
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////
    
    public Function<Object, ObjectAdapter> pojoToAdapter() {
        return IsisContext.pojoToAdapter();
    }
    
    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    public SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected ServiceRegistry getServiceRegistry() {
        return IsisContext.getServiceRegistry();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    @Inject WicketViewerSettings settings;
    protected WicketViewerSettings getSettings() {
        return settings;
    }


}
