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

package org.apache.isis.alternatives.objectstore.nosql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Identified;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverserDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorNoop;
import org.apache.isis.core.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.core.runtime.persistence.adapterfactory.AdapterFactoryAbstract;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.core.runtime.testsystem.TestClassSubstitutor;
import org.apache.isis.core.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.defaults.progmodel.JavaReflector;
import org.apache.isis.defaults.progmodel.ProgrammingModelFacetsJava5;

public class TrialObjects {

    private AdapterFactory factory;
    private JavaReflector reflector;

    private final Map<Object, ObjectAdapter> adapters = new HashMap<Object, ObjectAdapter>();

    public TrialObjects() {

        PropertiesConfiguration configuration = new PropertiesConfiguration();

        reflector =
            new JavaReflector(configuration, new TestClassSubstitutor(), new CollectionTypeRegistryDefault(),
                new SpecificationTraverserDefault(), new ProgrammingModelFacetsJava5(), new HashSet<FacetDecorator>(),
                new MetaModelValidatorNoop());
        reflector.setRuntimeContext(new RuntimeContextNoRuntime() {
            @Override
            public ObjectAdapter adapterFor(Object pattern) {
                return adapters.get(pattern);
            }

            @Override
            public ObjectAdapter adapterFor(Object pojo, ObjectAdapter ownerAdapter, Identified identified) {
                if (adapters.get(pojo) != null) {
                    return adapters.get(pojo);
                } else {
                    return factory.createAdapter(pojo, null);
                }
            }

        });
        reflector.init();

        factory = new AdapterFactoryAbstract() {
            @Override
            public TestProxyAdapter createAdapter(Object pojo, Oid oid) {
                ObjectSpecification specification = reflector.loadSpecification(pojo.getClass());
                ResolveState state =
                    oid == null ? ResolveState.VALUE : oid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST;

                final TestProxyAdapter testProxyObjectAdapter = new TestProxyAdapter();
                testProxyObjectAdapter.setupResolveState(state);
                testProxyObjectAdapter.setupObject(pojo);
                testProxyObjectAdapter.setupOid(oid);
                testProxyObjectAdapter.setupSpecification(specification);

                adapters.put(pojo, testProxyObjectAdapter);

                return testProxyObjectAdapter;
            }
        };
    }

    public ObjectSpecification loadSpecification(Class<? extends Object> cls) {
        return reflector.loadSpecification(cls);
    }

    public ObjectAdapter createAdapter(Object pojo, SerialOid oid) {
        return factory.createAdapter(pojo, oid);
    }
}
