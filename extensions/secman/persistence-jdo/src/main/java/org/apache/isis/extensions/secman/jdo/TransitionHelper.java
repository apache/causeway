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
package org.apache.isis.extensions.secman.jdo;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;

@DomainService(nature=NatureOfService.DOMAIN)
public class TransitionHelper {
	
    @Inject SpecificationLoader specificationLoader;
    @Inject FactoryService factoryService;

	public static ApplicationFeatureId defaultFeatureId() {
		return new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "default");
	}

	public static <T> T lookupService(Class<T> serviceClass) {
		return IsisContext.getServiceRegistry().lookupService(serviceClass).orElse(null);
	}
	
	public static String join(Object ... args) {
		try {
			return _NullSafe.stream(args)
			.map(arg->""+arg)
			.collect(Collectors.joining(":"));
		} catch (Exception e) {
			debug(args);
			throw e;
		}
	}
	
	private static void debug(Object ...x) {
    	for(int i=0;i<x.length;++i) {
    		System.out.println("debug["+i+"]: "+x[i]);
    	}
    }
	
    public <T extends ViewModel> T newViewModelInstance(Class<T> ofClass, String memento) {

		if(ofClass == null) {
			// TODO: [origin] not sure why, yet...
			return null;
		}

		final ObjectSpecification spec = specificationLoader.loadSpecification(ofClass);
		if (!spec.containsFacet(ViewModelFacet.class)) {
			throw new IsisException("Type must be a ViewModel: " + ofClass);
		}
//		final ManagedObject adapter = persistenceSessionServiceInternal.createViewModelInstance(spec, memento);
//		if(adapter.getSpecification().isViewModel()) {
//		    return (T)adapter.getPojo();
//		} else {
//			throw new IsisException("Object instantiated but was not given a ViewModel Oid; please report as a possible defect in Isis: " + ofClass);
//		}

		final T viewModel = factoryService.instantiate(ofClass);
		viewModel.viewModelInit(memento);
		return viewModel;
	}

}
