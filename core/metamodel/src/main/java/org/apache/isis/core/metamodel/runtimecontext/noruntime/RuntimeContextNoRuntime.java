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


package org.apache.isis.core.metamodel.runtimecontext.noruntime;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjectorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServices;
import org.apache.isis.core.metamodel.runtimecontext.AdapterMap;
import org.apache.isis.core.metamodel.runtimecontext.ObjectDirtier;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiator;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistor;
import org.apache.isis.core.metamodel.runtimecontext.QuerySubmitter;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesProvider;
import org.apache.isis.core.metamodel.runtimecontext.ServicesProviderAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class RuntimeContextNoRuntime extends RuntimeContextAbstract {

	private DependencyInjector dependencyInjector;

    public RuntimeContextNoRuntime() {
        dependencyInjector = new DependencyInjectorAbstract() {
            
            /**
             * Unlike most of the methods in this implementation, does nothing (because
             * this will always be called, even in a no-runtime context).
             */
            @Override
            public void injectDependenciesInto(Object domainObject) {
                
            }
        };
	}


	/////////////////////////////////////////////
	// Components
	/////////////////////////////////////////////

    @Override
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }
    
    @Override
    public AdapterMap getAdapterMap() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectInstantiator getObjectInstantiator() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }
    
    @Override
    public ObjectDirtier getObjectDirtier() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectPersistor getObjectPersistor() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }

    @Override
    public DomainObjectServices getDomainObjectServices() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }

    @Override
    public QuerySubmitter getQuerySubmitter() {
        throw new UnsupportedOperationException(
        "Not supported by this implementation of RuntimeContext");
    }

    @Override
    public DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }
    
	
	/////////////////////////////////////////////
	// allInstances, allMatching*
	/////////////////////////////////////////////

	public List<ObjectAdapter> allInstances(ObjectSpecification noSpec) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////

	@Override
    public ServicesProvider getServicesProvider() {
	    return new ServicesProviderAbstract() {
	        /**
	         * Just returns an empty array.
	         */
	        @Override
            public List<ObjectAdapter> getServices() {
	            return Collections.emptyList();
	        }
	    };
	}





}
