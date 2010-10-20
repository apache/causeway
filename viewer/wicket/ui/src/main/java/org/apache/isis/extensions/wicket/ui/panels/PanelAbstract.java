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


package org.apache.isis.extensions.wicket.ui.panels;

import java.util.List;

import org.apache.isis.extensions.wicket.model.nof.AuthenticationSessionAccessor;
import org.apache.isis.extensions.wicket.model.nof.PersistenceSessionAccessor;
import org.apache.isis.extensions.wicket.ui.ComponentType;
import org.apache.isis.extensions.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.extensions.wicket.ui.util.Components;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.inject.Inject;

/**
 * Convenience adapter for {@link Panel}s built up using {@link ComponentType}s.
 */
public abstract class PanelAbstract<T extends IModel<?>> extends Panel
		implements IHeaderContributor, PersistenceSessionAccessor, AuthenticationSessionAccessor {

	private static final long serialVersionUID = 1L;

	private ComponentType componentType;

	/**
     * Injected
     * @see #setComponentFactoryRegistry(ComponentFactoryRegistry)
     */
    private ComponentFactoryRegistry componentFactoryRegistry;

    
	public PanelAbstract(ComponentType componentType) {
		this(componentType, null);
	}

	public PanelAbstract(String id) {
		this(id, null);
	}

	public PanelAbstract(ComponentType componentType, T model) {
		this(componentType.getWicketId(), model);
	}

	public PanelAbstract(String id, T model) {
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
	protected T getModel() {
		return (T) getDefaultModel();
	}

	/**
	 * For subclasses
	 * 
	 * @return
	 */
	protected Component addOrReplace(ComponentType componentType,
			IModel<?> model) {
		return componentFactoryRegistry.addOrReplaceComponent(this,
				componentType, model);
	}

	/**
	 * For subclasses
	 */
	protected void permanentlyHide(ComponentType... componentIds) {
		Components.permanentlyHide(this, componentIds);
	}

	/**
	 * For subclasses
	 */
	public void permanentlyHide(String... ids) {
		Components.permanentlyHide(this, ids);
	}

	// ///////////////////////////////////////////////////////////////////
	// Header Contributors
	// ///////////////////////////////////////////////////////////////////

    /**
     * Automatically reference any corresponding CSS.
     */
	@Override
	public void renderHead(IHeaderResponse response) {
		renderHead(response, this.getClass());
	}

	/**
	 * Factored out to allow non-concrete subclasses to additionally render
	 * their own CSS if required.
	 */
	protected void renderHead(IHeaderResponse response, final Class<?> cls) {
		String url = cls.getSimpleName() + ".css";
		response.renderCSSReference(new ResourceReference(cls, url));
	}

	// ///////////////////////////////////////////////////////////////////
	// Convenience
	// ///////////////////////////////////////////////////////////////////




	/**
	 * The underlying {@link AuthenticationSession Isis session}
	 * wrapped in the {@link #getWebSession() Wicket session}.
	 * 
	 * @return
	 */
	public AuthenticationSession getAuthenticationSession() {
		final AuthenticationSessionAccessor asa = (AuthenticationSessionAccessor) Session.get();
        return asa.getAuthenticationSession();
	}

	// ///////////////////////////////////////////////////////////////////
	// Dependencies (from IsisContext
	// ///////////////////////////////////////////////////////////////////

	public IsisContext getIsisContext() {
		return IsisContext.getInstance();
	}
	
	public PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

	protected List<ObjectAdapter> getServiceAdapters() {
		return getPersistenceSession().getServices();
	}

	protected OidStringifier getOidStringifier() {
		return getPersistenceSession().getOidGenerator().getOidStringifier();
	}
	
    ///////////////////////////////////////////////////
    // Dependency Injection
    ///////////////////////////////////////////////////
    
    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return componentFactoryRegistry;
    }
    @Inject
    public void setComponentFactoryRegistry(
            ComponentFactoryRegistry componentFactoryRegistry) {
        this.componentFactoryRegistry = componentFactoryRegistry;
    }
	
}
