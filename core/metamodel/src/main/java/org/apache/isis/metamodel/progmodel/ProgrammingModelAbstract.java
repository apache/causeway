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

package org.apache.isis.metamodel.progmodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;

public abstract class ProgrammingModelAbstract implements ProgrammingModel {

    private final List<FacetFactory> facetFactories = _Lists.newArrayList();
    private final List<Object> facetFactoryInstancesOrClasses = _Lists.newLinkedList();

    public static final String KEY_IGNORE_DEPRECATED = "isis.reflector.facets.ignoreDeprecated";

    public enum DeprecatedPolicy {
        IGNORE,
        HONOUR;

        public static DeprecatedPolicy parse(final IsisConfigurationLegacy configuration) {
            boolean ignoreDep = configuration.getBoolean(KEY_IGNORE_DEPRECATED, false);
            return ignoreDep ? IGNORE : HONOUR;
        }

        //[2112]        
        //        public static DeprecatedPolicy parse(final IsisConfigurationBuilder configuration) {
        //            boolean ignoreDep = configuration.peekAtBoolean(KEY_IGNORE_DEPRECATED, false);
        //            return ignoreDep ? IGNORE : HONOUR;
        //        }

    }

    protected final DeprecatedPolicy deprecatedPolicy;

    public ProgrammingModelAbstract(
            final ProgrammingModelFacetsJava8.DeprecatedPolicy deprecatedPolicy) {

        this.deprecatedPolicy = deprecatedPolicy;
    }

    @Override
    public void init() {
        initializeIfRequired();
    }

    private void initializeIfRequired() {
        if(!facetFactories.isEmpty()) {
            return;
        }
        initialize();
    }

    private void initialize() {
        for (final Object factoryInstanceOrClass : facetFactoryInstancesOrClasses) {
            final FacetFactory facetFactory = asFacetFactory(factoryInstanceOrClass);
            facetFactories.add(facetFactory);
        }
    }

    private static FacetFactory asFacetFactory(final Object factoryInstanceOrClass) {
        if(factoryInstanceOrClass instanceof FacetFactory) {
            return (FacetFactory) factoryInstanceOrClass;
        } else {
            @SuppressWarnings("unchecked") final
            Class<? extends FacetFactory> factoryClass = (Class<? extends FacetFactory>) factoryInstanceOrClass;
            return (FacetFactory) InstanceUtil.createInstance(factoryClass);
        }
    }

    private void assertNotInitialized() {
        if(!facetFactories.isEmpty()) {
            throw new IllegalStateException("Programming model already initialized");
        }
    }


    @Override
    public final List<FacetFactory> getList() {
        initializeIfRequired();
        return Collections.unmodifiableList(facetFactories);
    }

    @Override
    public final void addFactory(final Class<? extends FacetFactory> factoryClass) {
        addFactory(factoryClass, Position.END);
    }

    @Override
    public final void addFactory(final Class<? extends FacetFactory> factoryClass, final Position position) {
        addFactory((Object)factoryClass, position);
    }

    @Override
    public void addFactory(final FacetFactory facetFactory) {
        addFactory(facetFactory, Position.END);
    }

    @Override
    public void addFactory(final FacetFactory facetFactory, final Position position) {
        addFactory((Object)facetFactory, position);
    }

    private void addFactory(final Object facetFactoryInstanceOrClass, final Position position) {
        assertNotInitialized();
        if(deprecatedPolicy == DeprecatedPolicy.IGNORE) {
            if( facetFactoryInstanceOrClass instanceof FacetFactory) {
                if(facetFactoryInstanceOrClass instanceof DeprecatedMarker) {
                    return;
                }
            } else if (facetFactoryInstanceOrClass instanceof Class) {
                if(DeprecatedMarker.class.isAssignableFrom((Class<?>)facetFactoryInstanceOrClass)) {
                    return;
                }
            }
        }
        switch (position){
        case BEGINNING:
            facetFactoryInstancesOrClasses.add(0, facetFactoryInstanceOrClass);
            break;
        case END:
            facetFactoryInstancesOrClasses.add(facetFactoryInstanceOrClass);
        }
    }


    @Override
    public final void removeFactory(final Class<? extends FacetFactory> factoryClass) {
        assertNotInitialized();
        for (Iterator<Object> iterator = facetFactoryInstancesOrClasses.iterator(); iterator.hasNext(); ) {
            final Object factoryInstanceOrClass = iterator.next();
            if(factoryInstanceOrClass == factoryClass || factoryClass.isAssignableFrom(factoryInstanceOrClass.getClass())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        for (final FacetFactory facetFactory : getList()) {
            if(facetFactory instanceof MetaModelValidatorRefiner) {
                final MetaModelValidatorRefiner metaModelValidatorRefiner = (MetaModelValidatorRefiner) facetFactory;
                metaModelValidatorRefiner.refineMetaModelValidator(metaModelValidator);
            }
        }
    }
}
