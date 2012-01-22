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

package org.apache.isis.core.metamodel.progmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facets.FacetFactory;

public abstract class ProgrammingModelAbstract implements ProgrammingModel {

    private final List<FacetFactory> facetFactories = new ArrayList<FacetFactory>();
    private final List<Class<? extends FacetFactory>> facetFactoryClasses = new ArrayList<Class<? extends FacetFactory>>();

    @Override
    public final List<FacetFactory> getList() {
        return Collections.unmodifiableList(facetFactories);
    }

    @Override
    public final void addFactory(final Class<? extends FacetFactory> factoryClass) {
        facetFactoryClasses.add(factoryClass);
    }

    @Override
    public final void removeFactory(final Class<? extends FacetFactory> factoryClass) {
        facetFactoryClasses.remove(factoryClass);
    }

    @Override
    public void init() {
        for (final Class<? extends FacetFactory> factoryClass : facetFactoryClasses) {
            final FacetFactory facetFactory = (FacetFactory) InstanceUtil.createInstance(factoryClass);
            facetFactories.add(facetFactory);
        }
    }

}
