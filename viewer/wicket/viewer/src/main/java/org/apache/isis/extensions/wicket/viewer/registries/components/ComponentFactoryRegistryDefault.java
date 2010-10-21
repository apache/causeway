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


package org.apache.isis.extensions.wicket.viewer.registries.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.extensions.wicket.ui.ComponentFactory;
import org.apache.isis.extensions.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.isis.extensions.wicket.ui.ComponentType;
import org.apache.isis.extensions.wicket.ui.app.registry.ComponentFactoryList;
import org.apache.isis.extensions.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Singleton;

/**
 * Implementation of {@link ComponentFactoryRegistry} that delegates to a
 * provided {@link ComponentFactoryList}.
 */
@Singleton
public class ComponentFactoryRegistryDefault implements
		ComponentFactoryRegistry {

	private final Multimap<ComponentType, ComponentFactory> componentFactoriesByType;

	public ComponentFactoryRegistryDefault() {
		this(new ComponentFactoryListDefault());
	}

	public ComponentFactoryRegistryDefault(
			ComponentFactoryList componentFactoryList) {
		componentFactoriesByType = Multimaps.newListMultimap(
				new HashMap<ComponentType, Collection<ComponentFactory>>(),
				new Supplier<List<ComponentFactory>>() {
					public List<ComponentFactory> get() {
						return Lists.<ComponentFactory> newArrayList();
					}
				});

		registerComponentFactories(componentFactoryList);
	}

	// ///////////////////////////////////////////////////////
	// Registration
	// ///////////////////////////////////////////////////////

	/**
	 * Registers the provided set of component factories.
	 */
	protected void registerComponentFactories(
			ComponentFactoryList componentFactoryList) {
		List<ComponentFactory> componentFactories = Lists.newArrayList();
		componentFactoryList.addComponentFactories(componentFactories);

		for (ComponentFactory componentFactory : componentFactories) {
			registerComponentFactory(componentFactory);
		}

		ensureAllComponentTypesRegistered();
	}

	protected synchronized void registerComponentFactory(
			ComponentFactory componentFactory) {
		componentFactoriesByType.put(componentFactory.getComponentType(),
				componentFactory);
	}

	private void ensureAllComponentTypesRegistered() {
		for (ComponentType componentType : ComponentType.values()) {
			Collection<ComponentFactory> componentFactories = componentFactoriesByType
					.get(componentType);
			if (componentFactories.isEmpty()) {
				throw new IllegalStateException(
						"No component factories registered for "
								+ componentType);
			}
		}
	}

	// ///////////////////////////////////////////////////////
	// Public API
	// ///////////////////////////////////////////////////////

	public Component addOrReplaceComponent(MarkupContainer markupContainer,
			ComponentType componentType, IModel<?> model) {
		Component component = createComponent(componentType, model);
		markupContainer.addOrReplace(component);
		return component;
	}

	@Override
	public Component addOrReplaceComponent(MarkupContainer markupContainer,
			String id, ComponentType componentType, IModel<?> model) {
		Component component = createComponent(componentType, id, model);
		markupContainer.addOrReplace(component);
		return component;
	}

	public Component createComponent(ComponentType componentType,
			IModel<?> model) {
		ComponentFactory componentFactory = findComponentFactoryElseFailFast(
				componentType, model);
		Component component = componentFactory.createComponent(model);
		return component;
	}

	@Override
	public Component createComponent(ComponentType componentType, String id,
			IModel<?> model) {
		ComponentFactory componentFactory = findComponentFactoryElseFailFast(
				componentType, model);
		Component component = componentFactory.createComponent(id, model);
		return component;
	}

	public List<ComponentFactory> findComponentFactories(
			final ComponentType componentType, final IModel<?> model) {
		Collection<ComponentFactory> componentFactoryList = componentFactoriesByType
				.get(componentType);
		final ArrayList<ComponentFactory> matching = new ArrayList<ComponentFactory>();
		for (ComponentFactory componentFactory : componentFactoryList) {
			final ApplicationAdvice appliesTo = componentFactory.appliesTo(componentType, model);
			if (appliesTo.applies()) {
				matching.add(componentFactory);
			}
			if (appliesTo.exclusively()) {
				break;
			}
		}
		return matching;
	}

	public ComponentFactory findComponentFactory(ComponentType componentType,
			IModel<?> model) {
		Collection<ComponentFactory> componentFactories = findComponentFactories(
				componentType, model);
		return firstOrNull(componentFactories);
	}

	public ComponentFactory findComponentFactoryElseFailFast(
			ComponentType componentType, IModel<?> model) {
		ComponentFactory componentFactory = findComponentFactory(componentType,
				model);
		if (componentFactory == null) {
			throw new RuntimeException(
					String
							.format(
									"could not find component for componentType = '%s'; model object is of type %s",
									componentType, model.getClass().getName()));
		}
		return componentFactory;
	}

	private static <T> T firstOrNull(Collection<T> collection) {
		Iterator<T> iterator = collection.iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

}
