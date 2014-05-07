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

package org.apache.isis.core.progmodel.facets.collections.event;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.event.PostsAddedToCollectionEventFacet;
import org.apache.isis.core.metamodel.facets.collections.event.PostsCollectionAddToEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;

public class PostsCollectionAddToEventFacetAnnotation extends
		PostsCollectionAddToEventFacetAbstract {

	private final CollectionAddToFacet collectionAddToFacet;
	private final CollectionFacet collectionFacet;
	private final ServicesProvider servicesProvider;

	private EventBusService eventBusService;
	private boolean searchedForEventBusService = false;
	private Class<? extends CollectionAddedToEvent<?, ?>> addedToCollectionEventType;

	public PostsCollectionAddToEventFacetAnnotation(
			Class<? extends CollectionAddedToEvent<?, ?>> addedToCollectionEventType,
			CollectionAddToFacet collectionAddToFacet,
			CollectionFacet collectionFacet,
			ServicesProvider servicesProvider, FacetHolder holder) {
		super(holder);
		this.addedToCollectionEventType = addedToCollectionEventType;
		this.collectionAddToFacet = collectionAddToFacet;
		this.collectionFacet = collectionFacet;
		this.servicesProvider = servicesProvider;
	}

	@Override
	public void add(ObjectAdapter inObject, ObjectAdapter value) {
		if (this.collectionAddToFacet == null) {
			return;
		}
		eventBusService = getEventBusService();
		if (eventBusService == null) {
			collectionAddToFacet.add(inObject, value);
			return;
		}

		final Boolean previouslyExisting = this.collectionFacet.contains(
				inObject, value);
		if (!previouslyExisting) {
			collectionAddToFacet.add(inObject, value);
			postEvent(inObject, value);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void postEvent(ObjectAdapter inObject,
			final Object addedValue) {
		final Object source = inObject.getObject();
		try {
			final Class type =  addedToCollectionEventType;
			final CollectionAddedToEvent<?, ?> event = newEvent(type, addedValue, source);

			eventBusService.post(event);
		} catch (Exception e) {
			throw new FatalException(e);
		}
	}


	static <S, T> CollectionAddedToEvent<S, T> newEvent(
			final Class<? extends CollectionAddedToEvent<S, T>> type,
			final T addedValue, final S source)
			throws InstantiationException, IllegalAccessException,
			NoSuchFieldException {
		final CollectionAddedToEvent<S, T> event = type.newInstance();

		setField("source", event, source);
		setField("addedValue", event, addedValue);
		return event;
	}

	private static void setField(final String name,
			final CollectionAddedToEvent<?, ?> event, final Object sourceValue)
			throws NoSuchFieldException, IllegalAccessException {
		final Field sourceField = CollectionAddedToEvent.class
				.getDeclaredField(name);
		sourceField.setAccessible(true);
		sourceField.set(event, sourceValue);
	}

	private EventBusService getEventBusService() {
		if (!searchedForEventBusService) {
			final List<ObjectAdapter> serviceAdapters = servicesProvider
					.getServices();
			for (ObjectAdapter serviceAdapter : serviceAdapters) {
				final Object service = serviceAdapter.getObject();
				if (service instanceof EventBusService) {
					eventBusService = (EventBusService) service;
					break;
				}
			}
		}
		searchedForEventBusService = true;
		return eventBusService;
	}

	// //////////////////////////////////////
	// MultiTypedFacet

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Facet>[] facetTypes() {
		return Lists.newArrayList(PostsAddedToCollectionEventFacet.class,
				CollectionAddToFacet.class).toArray(
				new Class[] {});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Facet> T getFacet(Class<T> facet) {
		return (T) this;
	}



}
