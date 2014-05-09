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
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.WrapperPolicy;
import org.apache.isis.applib.services.eventbus.CollectionRemovedFromEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.event.PostsCollectionRemovedFromEventFacet;
import org.apache.isis.core.metamodel.facets.collections.event.PostsCollectionRemovedFromEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;

public class PostsCollectionRemovedFromEventFacetAnnotation extends
		PostsCollectionRemovedFromEventFacetAbstract {

	private final PropertyOrCollectionAccessorFacet getterFacet;
	private final CollectionRemoveFromFacet collectionRemoveFromFacet;
	private final ServicesProvider servicesProvider;

	private EventBusService eventBusService;
	private boolean searchedForEventBusService = false;

	public PostsCollectionRemovedFromEventFacetAnnotation(
			final Class<? extends CollectionRemovedFromEvent<?, ?>> eventType,
			final WrapperPolicy wrapperPolicy,
			final PropertyOrCollectionAccessorFacet getterFacet,
			final CollectionRemoveFromFacet collectionRemoveFromFacet,
			final ServicesProvider servicesProvider, final FacetHolder holder) {
		super(eventType, wrapperPolicy, holder);
		this.getterFacet = getterFacet;
		this.collectionRemoveFromFacet = collectionRemoveFromFacet;
		this.servicesProvider = servicesProvider;
	}

	@Override
	public void remove(ObjectAdapter targetAdapter,
			ObjectAdapter referencedObjectAdapter) {
		if (this.collectionRemoveFromFacet == null) {
			return;
		}
		eventBusService = getEventBusService();
		if (eventBusService == null) {
			collectionRemoveFromFacet.remove(targetAdapter,
					referencedObjectAdapter);
			return;
		}

		final Object referencedObject = AdapterUtils
				.unwrap(referencedObjectAdapter);

		// get hold of underlying collection
		final Object collection = getterFacet.getProperty(targetAdapter);

		// don't post event if the collections does not contain object
		if (!((Collection<?>) collection).contains(referencedObject)) {
			return;
		}

		// contains the element. So the event must be posted.
		collectionRemoveFromFacet
				.remove(targetAdapter, referencedObjectAdapter);

		postEvent(targetAdapter, getIdentified().getIdentifier(),
				referencedObject);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void postEvent(final ObjectAdapter targetAdapter,
			final Identifier identifier, final Object removedReference) {

		final Object source = targetAdapter.getObject();
		try {
			final Class type = value();
			final CollectionRemovedFromEvent<?, ?> event = newEvent(type, source,
					identifier, removedReference);
			eventBusService.post(event);
		} catch (Exception e) {
			throw new FatalException(e);
		}
	}

	static <S, T> CollectionRemovedFromEvent<S, T> newEvent(
			final Class<? extends CollectionRemovedFromEvent<S, T>> type,
			final S source, final Identifier identifier, final T value)
			throws InstantiationException, IllegalAccessException,
			NoSuchFieldException {
		final CollectionRemovedFromEvent<S, T> event = type.newInstance();

		setField("source", event, source);
		setField("identifier", event, identifier);
		setField("value", event, value);
		return event;
	}

	private static void setField(final String name,
			final CollectionRemovedFromEvent<?, ?> event, final Object sourceValue)
			throws NoSuchFieldException, IllegalAccessException {
		final Field sourceField = CollectionRemovedFromEvent.class
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
	// //////////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Facet>[] facetTypes() {
		return Lists.newArrayList(type(), // ie CollectionRemoveFromFacet
				PostsCollectionRemovedFromEventFacet.class).toArray(new Class[] {});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Facet> T getFacet(Class<T> facet) {
		return (T) this;
	}

}
