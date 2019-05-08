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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

public abstract class RecreatableObjectFacetDeclarativeInitializingAbstract 
extends RecreatableObjectFacetAbstract {

	public RecreatableObjectFacetDeclarativeInitializingAbstract(
			final FacetHolder holder,
			final RecreationMechanism recreationMechanism,
			final PostConstructMethodCache postConstructMethodCache) {
		super(holder, recreationMechanism, postConstructMethodCache);
	}

	private UrlEncodingService codec;
	private SerializingAdapter serializer; 

	@Override
	protected void doInitialize(
			final Object viewModelPojo,
			final String mementoStr) {

		final _Mementos.Memento memento = parseMemento(mementoStr);

		final Set<String> mementoKeys = memento.keySet();

		final ObjectAdapter viewModelAdapter = getObjectAdapterProvider()
				.adapterForViewModel(viewModelPojo, mementoStr);
		viewModelAdapter.injectServices(getServiceInjector());

		final ObjectSpecification spec = viewModelAdapter.getSpecification();
		final Stream<OneToOneAssociation> properties = spec.streamProperties(Contributed.EXCLUDED);

		properties.forEach(property->{
			final String propertyId = property.getId();

			Object propertyValue = null;

			if(mementoKeys.contains(propertyId)) {
				final Class<?> propertyType = property.getSpecification().getCorrespondingClass();
				propertyValue = memento.get(propertyId, propertyType);
			}

			if(propertyValue != null) {
				property.set(viewModelAdapter, getObjectAdapterProvider().adapterFor(propertyValue), InteractionInitiatedBy.FRAMEWORK);
			}
		});

	}

	@Override
	public String memento(Object viewModelPojo) {

		final _Mementos.Memento memento = newMemento();

		final ManagedObject ownerAdapter = 
	    /*
	     * ObjectAdapter that holds the ObjectSpecification used for 
	     * interrogating the domain object's metadata. 
	     * 
	     * Does _not_ perform dependency injection on the domain object. Also bypasses 
	     * caching (if any), that is each call to this method creates a new unique instance.
	     */
	    ManagedObject.of(getSpecificationLoader().loadSpecification(viewModelPojo.getClass()), viewModelPojo);
		
		
		final ObjectSpecification spec = ownerAdapter.getSpecification();

		final Stream<OneToOneAssociation> properties = spec.streamProperties(Contributed.EXCLUDED);

		properties
		// ignore read-only
		.filter(property->property.containsDoOpFacet(PropertySetterFacet.class)) 
		// ignore those explicitly annotated as @NotPersisted
		.filter(property->!property.isNotPersisted())
		.forEach(property->{
			final ManagedObject propertyValue = 
					property.get2(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
			if(propertyValue != null) {
				memento.put(property.getId(), propertyValue.getPojo());
			}
		});

		return memento.asString();
	}

	// -- HELPER

	private void initDependencies() {
		val serviceRegistry = getServiceRegistry();
		this.codec = serviceRegistry.lookupServiceElseFail(UrlEncodingService.class);
		this.serializer = serviceRegistry.lookupServiceElseFail(SerializingAdapter.class);
	}

	private void ensureDependenciesInited() {
		if(codec==null) {
			initDependencies();
		}
	}

	private _Mementos.Memento newMemento() {
		ensureDependenciesInited();
		return _Mementos.create(codec, serializer);
	}

	private _Mementos.Memento parseMemento(String input) {
		ensureDependenciesInited();
		return _Mementos.parse(codec, serializer, input);
	}


}
