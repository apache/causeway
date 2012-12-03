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

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;

import com.google.common.collect.Lists;

public abstract class ProgrammingModelAbstract implements ProgrammingModel {

    private final List<FacetFactory> facetFactories = Lists.newArrayList();
    private final List<Object> facetFactoryInstancesOrClasses = Lists.newArrayList();

    @Override
    public void init() {
        for (final Object factoryInstanceOrClass : facetFactoryInstancesOrClasses) {
            final FacetFactory facetFactory = asFacetFactory(factoryInstanceOrClass);
            facetFactories.add(facetFactory);
        }
    }

    private static FacetFactory asFacetFactory(final Object factoryInstanceOrClass) {
        if(factoryInstanceOrClass instanceof FacetFactory) {
            return (FacetFactory) factoryInstanceOrClass;
        } else {
            @SuppressWarnings("unchecked")
            Class<? extends FacetFactory> factoryClass = (Class<? extends FacetFactory>) factoryInstanceOrClass;
            return (FacetFactory) InstanceUtil.createInstance(factoryClass);
        }
    }

    @Override
    public final List<FacetFactory> getList() {
        return Collections.unmodifiableList(facetFactories);
    }

    @Override
    public final void addFactory(final Class<? extends FacetFactory> factoryClass) {
        facetFactoryInstancesOrClasses.add(factoryClass);
    }

    @Override
    public final void removeFactory(final Class<? extends FacetFactory> factoryClass) {
        facetFactoryInstancesOrClasses.remove(factoryClass);
    }

    @Override
    public void addFactory(FacetFactory facetFactory) {
        facetFactoryInstancesOrClasses.add(facetFactory);
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        for (FacetFactory facetFactory : getList()) {
            if(facetFactory instanceof MetaModelValidatorRefiner) {
                MetaModelValidatorRefiner metaModelValidatorRefiner = (MetaModelRefiner) facetFactory;
                metaModelValidatorRefiner.refineMetaModelValidator(metaModelValidator, configuration);
            }
        }
    }
}
