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


package org.apache.isis.metamodel.facets;

import java.util.List;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.ObjectReflector;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.metamodel.specloader.internal.cache.SpecificationCache;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;


public class ProgrammableReflector implements ObjectReflector {

	
    public void init() {}

    public void installServiceSpecification(final Class<?> cls) {}

    private ObjectSpecification[] allSpecificationsReturn;

    public void setAllSpecificationsReturn(final ObjectSpecification[] allSpecificationsReturn) {
        this.allSpecificationsReturn = allSpecificationsReturn;
    }

    public ObjectSpecification[] allSpecifications() {
        return allSpecificationsReturn;
    }

    private CollectionTypeRegistry getCollectionTypeRegistryReturn;

    public void setGetCollectionTypeRegistryReturn(final CollectionTypeRegistry getCollectionTypeRegistryReturn) {
        this.getCollectionTypeRegistryReturn = getCollectionTypeRegistryReturn;
    }

    public CollectionTypeRegistry getCollectionTypeRegistry() {
        return getCollectionTypeRegistryReturn;
    }

    public ObjectSpecification loadSpecification(final Class<?> type) {
        return loadSpecification(type.getName());
    }

    private ObjectSpecification loadSpecificationStringReturn;

    public void setLoadSpecificationStringReturn(final ObjectSpecification loadSpecificationStringReturn) {
        this.loadSpecificationStringReturn = loadSpecificationStringReturn;
    }

    public ObjectSpecification loadSpecification(final String name) {
        return loadSpecificationStringReturn;
    }

    public void shutdown() {}

    public void setCache(SpecificationCache cache) {
        throw new NotYetImplementedException();
    }

    public boolean loaded(Class<?> cls) {
        throw new NotYetImplementedException();
    }

    public boolean loaded(String fullyQualifiedClassName) {
        throw new NotYetImplementedException();
    }

    public void injectInto(Object candidate) {}

    public ClassSubstitutor getClassSubstitutor() {
        return null;
    }

	public void setRuntimeContext(RuntimeContext runtimeContext) {
        // ignored
	}

	public RuntimeContext getRuntimeContext() {
        throw new NotYetImplementedException();
	}

    public void debugData(DebugString debug) {}

    public String debugTitle() {
        return null;
    }

	public void setServiceClasses(List<Class<?>> serviceClasses) {
		throw new NotYetImplementedException();
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

