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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAbstract;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverserDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorNoop;
import org.apache.isis.core.progmodel.layout.dflt.MemberLayoutArrangerDefault;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactoryAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestClassSubstitutor;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyAdapter;

public class TrialObjects {

    private AdapterFactory factory;
    private ObjectReflectorDefault reflector;

    private final Map<Object, ObjectAdapter> adapters = Maps.newHashMap();

    public TrialObjects() {

        IsisConfigurationDefault configuration = new IsisConfigurationDefault();

        final AdapterMapAbstract adapterMap = new AdapterMapAbstract() {
            
            @Override
            public ObjectAdapter getAdapterFor(Object pojo) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public ObjectAdapter adapterFor(Object pojo, ObjectAdapter ownerAdapter, IdentifiedHolder identifiedHolder) {
                if (adapters.get(pojo) != null) {
                    return adapters.get(pojo);
                } else {
                    return factory.createAdapter(pojo, null);
                }
            }
            
            @Override
            public ObjectAdapter adapterFor(Object pattern) {
                return adapters.get(pattern);
            }
        };

        reflector =
            new ObjectReflectorDefault(configuration, new TestClassSubstitutor(), new CollectionTypeRegistryDefault(),
                new SpecificationTraverserDefault(), new MemberLayoutArrangerDefault(), new ProgrammingModelFacetsJava5(), new HashSet<FacetDecorator>(),
                new MetaModelValidatorNoop());
        reflector.setRuntimeContext(new RuntimeContextNoRuntime() {

            @Override
            public AdapterMap getAdapterMap() {
                return adapterMap;
            }
        });
        reflector.init();

        factory = new AdapterFactoryAbstract() {
            @Override
            public ObjectAdapter createAdapter(Object pojo, Oid oid) {
                ObjectSpecification specification = reflector.loadSpecification(pojo.getClass());
                ResolveState state =
                    oid == null ? ResolveState.VALUE : oid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST;

                final TestProxyAdapter objectAdapter = new TestProxyAdapter();
                objectAdapter.setupResolveState(state);
                objectAdapter.setupObject(pojo);
                objectAdapter.setupOid(oid);
                objectAdapter.setupSpecification(specification);

                adapters.put(pojo, objectAdapter);

                return objectAdapter;
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
