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


package org.apache.isis.core.metamodel.runtimecontext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerAware;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderAware;


public abstract class RuntimeContextAbstract implements RuntimeContext, SpecificationLoaderAware, DomainObjectContainerAware {
	private SpecificationLoader specificationLoader;
	private DomainObjectContainer container;
	private Properties properties;

	public RuntimeContextAbstract() {
	}

	public void injectInto(Object candidate) {
        if (RuntimeContextAware.class.isAssignableFrom(candidate.getClass())) {
        	RuntimeContextAware cast = RuntimeContextAware.class.cast(candidate);
            cast.setRuntimeContext(this);
        }
	}

	
	public SpecificationLoader getSpecificationLoader() {
		return specificationLoader;
	}
	
	/**
	 * Is injected into when the reflector is {@link ObjectReflectorAbstract#init() initialized}.
	 */
	public void setSpecificationLoader(SpecificationLoader specificationLoader) {
		this.specificationLoader = specificationLoader;
	}
	
	

	protected DomainObjectContainer getContainer() {
		return container;
	}
	/**
	 * So that {@link #injectDependenciesInto(Object)} can also inject the {@link DomainObjectContainer}.
	 */
	public void setContainer(DomainObjectContainer container) {
		this.container = container;
	}
	
	public void setProperties(Properties properties) {
        this.properties = properties;
    }
	
	public String getProperty(String name) {
	    return properties.getProperty(name);
	}
	
	public List<String> getPropertyNames() {
	    List<String> list= new ArrayList<String>();
	    for (Object key : properties.keySet()) {
	        list.add((String) key);
        }
	    return list;
	}
	


}
