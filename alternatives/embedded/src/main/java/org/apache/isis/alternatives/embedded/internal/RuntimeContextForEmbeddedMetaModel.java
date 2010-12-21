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


package org.apache.isis.alternatives.embedded.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.alternatives.embedded.EmbeddedContext;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProviderAbstract;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjectorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServices;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServicesAbstract;
import org.apache.isis.core.metamodel.runtimecontext.AdapterMap;
import org.apache.isis.core.metamodel.runtimecontext.AdapterMapAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ObjectDirtier;
import org.apache.isis.core.metamodel.runtimecontext.ObjectDirtierAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiator;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiatorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistor;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.QuerySubmitter;
import org.apache.isis.core.metamodel.runtimecontext.QuerySubmitterAbstract;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesProvider;
import org.apache.isis.core.metamodel.runtimecontext.ServicesProviderAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification.CreationMode;
import org.apache.isis.core.metamodel.spec.identifier.Identified;

/**
 * Acts as a bridge between the {@link RuntimeContext} (as used internally
 * within the meta-model) and the {@link EmbeddedContext} 
 * provided by the embedder (which deals only with pojos).
 */
public class RuntimeContextForEmbeddedMetaModel extends RuntimeContextAbstract implements ApplicationScopedComponent {

	private final EmbeddedContext context;
	private final List<Object> services;
	private List<ObjectAdapter> serviceAdapters;
	private ServicesInjector servicesInjector;
    private final AdapterMap adapterMap;
    private final ObjectInstantiator objectInstantiator;
    private final ObjectDirtier objectDirtier;
    private final ObjectPersistor objectPersistor;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ServicesProvider servicesProvider;
    private final DomainObjectServices domainObjectServices;
    private final QuerySubmitter querySubmitter;
    private final DependencyInjector dependencyInjector;
    
	public RuntimeContextForEmbeddedMetaModel(
			final EmbeddedContext context, 
			final List<Object> services) {
		this.context = context;
		this.services = services;
		this.authenticationSessionProvider = new AuthenticationSessionProviderAbstract() {
		    @Override
		    public AuthenticationSession getAuthenticationSession() {
		        return context.getAuthenticationSession();
		    }
        };
        this.querySubmitter = new QuerySubmitterAbstract() {
            @Override
            public List<ObjectAdapter> allMatchingQuery(Query query) {
                return wrap(context.allMatchingQuery(query));
            }
            @Override
            public <T> ObjectAdapter firstMatchingQuery(Query<T> query) {
                return getAdapterMap().adapterFor(context.firstMatchingQuery(query));
            }

        };
		this.adapterMap = new AdapterMapAbstract() {
	        @Override
	        public ObjectAdapter adapterFor(Object domainObject) {
	            ObjectSpecification domainObjectSpec = getSpecificationLookup().loadSpecification(domainObject.getClass());
	            PersistenceState persistenceState = context.getPersistenceState(domainObject);
	            return new StandaloneAdapter(domainObjectSpec, domainObject, persistenceState);
	        }

	        @Override
	        public ObjectAdapter adapterFor(Object domainObject, ObjectAdapter ownerAdapter, Identified identified) {
	            return adapterFor(domainObject);
	        }

	        @Override
	        public ObjectAdapter getAdapterFor(Object domainObject) {
	            return adapterFor(domainObject);
	        }
	    };
        this.objectInstantiator = new ObjectInstantiatorAbstract() {
            
            @Override
            public Object instantiate(Class<?> type) throws ObjectInstantiationException {
                return context.instantiate(type);
            }
        };
        
        this.objectPersistor = new ObjectPersistorAbstract() {
            
            @Override
            public void makePersistent(ObjectAdapter adapter) {
                context.makePersistent(adapter.getObject());
            }

            @Override
            public void remove(ObjectAdapter adapter) {
                context.remove(adapter.getObject());
            }
            
        };
		this.objectDirtier = new ObjectDirtierAbstract(){

	        @Override
	        public void objectChanged(ObjectAdapter adapter) {
	            context.objectChanged(adapter.getObject());
	        }

	        @Override
	        public void objectChanged(Object object) {
	            context.objectChanged(object);
	        }
	        
	    };

        this.servicesProvider = new ServicesProviderAbstract() {
            @Override
            public List<ObjectAdapter> getServices() {
                return serviceAdapters;
            }
        };


	    this.domainObjectServices = new DomainObjectServicesAbstract() {

	        @Override
	        public ObjectAdapter createTransientInstance(ObjectSpecification spec) {
	            Object domainObject = spec.createObject(CreationMode.INITIALIZE);
	            return adapterMap.adapterFor(domainObject);
	        }

	        @Override
	        public void resolve(Object parent) {
	            context.resolve(parent);
	        }

	        @Override
	        public void resolve(Object parent, Object field) {
	            context.resolve(parent, field);
	        }

	        @Override
	        public boolean flush() {
	            return context.flush();
	        }
	        
	        @Override
	        public void commit() {
	            context.commit();
	        }

	        @Override
            public String getProperty(String name) {
                return RuntimeContextForEmbeddedMetaModel.this.getProperty(name);
            }

            @Override
            public List<String> getPropertyNames() {
                return RuntimeContextForEmbeddedMetaModel.this.getPropertyNames();
            }

            @Override
            public void informUser(String message) {
                context.informUser(message);
            }

            @Override
            public void warnUser(String message) {
                context.warnUser(message);
            }

            @Override
            public void raiseError(String message) {
                context.raiseError(message);
            }
        };
        this.dependencyInjector = new DependencyInjectorAbstract() {
            @Override
            public void injectDependenciesInto(Object domainObject) {
                if (servicesInjector == null) {
                    throw new IllegalStateException("must setContainer before using this method");
                }
                servicesInjector.injectDependencies(domainObject);
            }
        };
	}


	/////////////////////////////////////////////
	// init, shutdown
	/////////////////////////////////////////////

	@Override
    public void init() {
		this.serviceAdapters = adaptersFor(services);
		
		servicesInjector = new ServicesInjectorDefault();
		servicesInjector.setContainer(getContainer());
		servicesInjector.setServices(services);
	}
	

	@Override
    public void shutdown() {
		// does nothing
	}


	private List<ObjectAdapter> adaptersFor(List<Object> services) {
		List<ObjectAdapter> serviceAdapters = new ArrayList<ObjectAdapter>();
		for(Object service: services) {
			ObjectSpecification spec = getSpecificationLookup().loadSpecification(service.getClass());
			serviceAdapters.add(new ServiceAdapter(spec, service));
		}
		return Collections.unmodifiableList(serviceAdapters);
	}

	
	/////////////////////////////////////////////
	// Components
	/////////////////////////////////////////////

	@Override
	public AuthenticationSessionProvider getAuthenticationSessionProvider() {
	    return authenticationSessionProvider;
	}
	
    @Override
    public AdapterMap getAdapterMap() {
        return adapterMap;
    }

    @Override
    public ObjectInstantiator getObjectInstantiator() {
        return objectInstantiator;
    }
    
    @Override
    public ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

    @Override
    public ServicesProvider getServicesProvider() {
        return servicesProvider;
    }

    @Override
    public DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }

    @Override
    public DomainObjectServices getDomainObjectServices() {
        return domainObjectServices;
    }

    @Override
    public QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }
	

    @Override
    public ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

	
	/////////////////////////////////////////////
	// firstMatchingQuery
	/////////////////////////////////////////////

	private List<ObjectAdapter> wrap(List<?> pojos) {
		List<ObjectAdapter> adapters = new ArrayList<ObjectAdapter>();
		for(Object pojo: pojos) {
			adapters.add(getAdapterMap().adapterFor(pojo));
		}
		return adapters;
	}


	/////////////////////////////////////////////
	// getServices
	/////////////////////////////////////////////

	public ServicesInjector getServicesInjector() {
		return servicesInjector;
	}

}
