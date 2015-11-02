/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.isis.viewer.wicket.viewer.guice;

import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IBehaviorInstantiationListener;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.model.Model;

/**
 * Injects field members of components and behaviors using Guice.
 * <p>
 * Add this to your application in its {@link Application#init()} method like so:
 * 
 * <pre>
 * getComponentInstantiationListeners().add(new GuiceComponentInjector(this));
 * </pre>
 * 
 * <p>
 * There are different constructors for this object depending on how you want to wire things. See
 * the javadoc for the constructors for more information.
 * </p>
 * <p>
 * Only Wicket {@link Component}s and {@link Behavior}s are automatically injected, other classes
 * such as {@link Session}, {@link Model}, and any other POJO can be injected by calling
 * <code>Injector.get().inject(this)</code> in their constructor.
 * </p>
 * 
 * @author Alastair Maw
 */
public class GuiceComponentInjector extends org.apache.wicket.injection.Injector
	implements
		IComponentInstantiationListener,
		IBehaviorInstantiationListener
{
	private final IFieldValueFactory fieldValueFactory;

	/**
	 * Creates a new Wicket GuiceComponentInjector instance.
	 * <p>
	 * Internally this will create a new Guice {@link Injector} instance, with no {@link Module}
	 * instances. This is only useful if your beans have appropriate {@link ImplementedBy}
	 * annotations on them so that they can be automatically picked up with no extra configuration
	 * code.
	 * 
	 * @param app
	 */
	public GuiceComponentInjector(final Application app)
	{
		this(app, new Module[0]);
	}

	/**
	 * Creates a new Wicket GuiceComponentInjector instance, using the supplied Guice {@link Module}
	 * instances to create a new Guice {@link Injector} instance internally.
	 * 
	 * @param app
	 * @param modules
	 */
	public GuiceComponentInjector(final Application app, final Module... modules)
	{
		this(app, Guice.createInjector(app.usesDeploymentConfig() ? Stage.PRODUCTION
			: Stage.DEVELOPMENT, modules), true);
	}

	/**
	 * Constructor
	 * 
	 * @param app
	 * @param injector
	 */
	public GuiceComponentInjector(final Application app, final Injector injector)
	{
		this(app, injector, true);
	}

	/**
	 * Creates a new Wicket GuiceComponentInjector instance, using the provided Guice
	 * {@link Injector} instance.
	 * 
	 * @param app
	 * @param injector
	 * @param wrapInProxies
	 *            whether or not wicket should wrap dependencies with specialized proxies that can
	 *            be safely serialized. in most cases this should be set to true.
	 */
	public GuiceComponentInjector(final Application app, final Injector injector,
		final boolean wrapInProxies)
	{
		app.setMetaData(GuiceInjectorHolder.INJECTOR_KEY, new GuiceInjectorHolder(injector));
		fieldValueFactory = new GuiceFieldValueFactory(wrapInProxies);
		app.getBehaviorInstantiationListeners().add(this);
		bind(app);
	}

	@Override
	public void inject(final Object object)
	{
		inject(object, fieldValueFactory);
	}

	@Override
	public void onInstantiation(final Component component)
	{
		inject(component);
	}

	@Override
	public void onInstantiation(Behavior behavior)
	{
		inject(behavior);
	}
}
