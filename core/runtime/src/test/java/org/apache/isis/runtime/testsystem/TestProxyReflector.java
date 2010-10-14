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


package org.apache.isis.runtime.testsystem;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.services.ServicesInjector;
import org.apache.isis.metamodel.services.ServicesInjectorNoop;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.ObjectReflector;
import org.apache.isis.metamodel.specloader.SpecificationLoaderAware;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutorIdentity;
import org.apache.isis.metamodel.specloader.internal.cache.SpecificationCache;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.testspec.TestProxySpecification;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactoryBasic;


public class TestProxyReflector implements ObjectReflector {
	
    private final Hashtable<String,ObjectSpecification> specificationByFullName = new Hashtable<String,ObjectSpecification>();
    
    private ObjectFactory objectFactory = new ObjectFactoryBasic();
    private ClassSubstitutor classSubstitutor = new ClassSubstitutorIdentity();

    
    public TestProxyReflector() {
    	
    }
    
    public void init() {}
    public void shutdown() {}


    public ObjectSpecification[] allSpecifications() {
        ObjectSpecification[] specsArray;
        specsArray = new ObjectSpecification[specificationByFullName.size()];
        int i = 0;
        final Enumeration<ObjectSpecification> e = specificationByFullName.elements();
        while (e.hasMoreElements()) {
            specsArray[i++] = (ObjectSpecification) e.nextElement();
        }
        return specsArray;
    }

    public void debugData(final DebugString debug) {
        final ObjectSpecification[] list = allSpecifications();
        for (int i = 0; i < list.length; i++) {
            debug.appendln(list[i].getFullName());
        }
    }

    public String debugTitle() {
        return null;
    }

    public void installServiceSpecification(final Class<?> class1) {}

    public ObjectSpecification loadSpecification(final Class<?> type) {
        return loadSpecification(type.getName());
    }

    public ObjectSpecification loadSpecification(final String name) {
        if (specificationByFullName.containsKey(name)) {
            return (ObjectSpecification) specificationByFullName.get(name);
        } else {
            final TestProxySpecification specification = new TestProxySpecification(name);
            specificationByFullName.put(specification.getFullName(), specification);
            return specification;

            // throw new ObjectAdapterRuntimeException("no specification for " + name);
        }
    }


    public ObjectAdapter createCollectionAdapter(final Object collection, final ObjectSpecification elementSpecification) {
        return null;
    }

    public ServicesInjector createServicesInjector() {
        return new ServicesInjectorNoop();
    }

    public void addSpecification(final ObjectSpecification specification) {
        specificationByFullName.put(specification.getFullName(), specification);
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setCache(SpecificationCache cache) {
        // ignored.
    }

    public void setObjectPersistor(PersistenceSession objectPersistor) {
        // ignored.
    }

    public boolean loaded(Class<?> cls) {
        return false;
    }

    public boolean loaded(String fullyQualifiedClassName) {
        return false;
    }
    
    
    public void injectInto(Object candidate) {
        if (SpecificationLoaderAware.class.isAssignableFrom(candidate.getClass())) {
            SpecificationLoaderAware cast = SpecificationLoaderAware.class.cast(candidate);
            cast.setSpecificationLoader(this);
        }
    }
    public ClassSubstitutor getClassSubstitutor() {
        return classSubstitutor;
    }
    
	public void setRuntimeContext(RuntimeContext runtimeContext) {
        // ignored
	}

	public RuntimeContext getRuntimeContext() {
        throw new NotYetImplementedException();
	}

	public void setServiceClasses(List<Class<?>> serviceClasses) {
        // ignored.
	}
	public MetaModelValidator getMetaModelValidator() {
		throw new NotYetImplementedException();
	}
	public ProgrammingModelFacets getProgrammingModelFacets() {
		throw new NotYetImplementedException();
	}
	public SpecificationTraverser getSpecificationTraverser() {
		throw new NotYetImplementedException();
	}

}
