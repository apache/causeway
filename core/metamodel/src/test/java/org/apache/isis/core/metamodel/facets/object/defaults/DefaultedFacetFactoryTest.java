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

package org.apache.isis.core.metamodel.facets.object.defaults;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.annotation.Defaulted;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.defaults.annotcfg.DefaultedFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class DefaultedFacetFactoryTest extends AbstractFacetFactoryTest {

    private DefaultedFacetAnnotationElseConfigurationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DefaultedFacetAnnotationElseConfigurationFactory();

        ServicesInjector servicesInjector = ServicesInjector.builderForTesting().build();
        facetFactory.setServicesInjector(servicesInjector);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFacetPickedUp() {
        facetFactory.process(new ProcessClassContext(MyDefaultedUsingDefaultsProvider.class, methodRemover, facetedMethod));

        final DefaultedFacet facet = facetedMethod.getFacet(DefaultedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DefaultedFacetAbstract);
    }

    public void testFacetFacetHolderStored() {

        facetFactory.process(new ProcessClassContext(MyDefaultedUsingDefaultsProvider.class, methodRemover, facetedMethod));

        final DefaultedFacetAbstract valueFacet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertEquals(facetedMethod, valueFacet.getFacetHolder());
    }

    public void testNoMethodsRemoved() {

        facetFactory.process(new ProcessClassContext(MyDefaultedUsingDefaultsProvider.class, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

    abstract static class DefaultsProviderNoop<T> implements DefaultsProvider<T> {

        @Override
        public abstract T getDefaultValue();

    }

    @Defaulted(defaultsProviderName = "org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetFactoryTest$MyDefaultedUsingDefaultsProvider")
    public static class MyDefaultedUsingDefaultsProvider extends DefaultsProviderNoop<MyDefaultedUsingDefaultsProvider> {

        /**
         * Required since is a DefaultsProvider.
         */
        public MyDefaultedUsingDefaultsProvider() {
        }

        @Override
        public MyDefaultedUsingDefaultsProvider getDefaultValue() {
            return new MyDefaultedUsingDefaultsProvider();
        }
    }

    public void testDefaultedUsingDefaultsProviderName() {
        facetFactory.process(new ProcessClassContext(MyDefaultedUsingDefaultsProvider.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertEquals(MyDefaultedUsingDefaultsProvider.class, facet.getDefaultsProviderClass());
    }

    @Defaulted(defaultsProviderClass = MyDefaultedUsingDefaultsProviderClass.class)
    public static class MyDefaultedUsingDefaultsProviderClass extends DefaultsProviderNoop<MyDefaultedUsingDefaultsProviderClass> {

        /**
         * Required since is a DefaultsProvider.
         */
        public MyDefaultedUsingDefaultsProviderClass() {
        }

        @Override
        public MyDefaultedUsingDefaultsProviderClass getDefaultValue() {
            return new MyDefaultedUsingDefaultsProviderClass();
        }
    }

    public void testDefaultedUsingDefaultsProviderClass() {
        facetFactory.process(new ProcessClassContext(MyDefaultedUsingDefaultsProviderClass.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertEquals(MyDefaultedUsingDefaultsProviderClass.class, facet.getDefaultsProviderClass());
    }

    public void testDefaultedMustBeADefaultsProvider() {
        // no test, because compiler prevents us from nominating a class that
        // doesn't
        // implement DefaultsProvider
    }

    @Defaulted(defaultsProviderClass = MyDefaultedWithoutNoArgConstructor.class)
    public static class MyDefaultedWithoutNoArgConstructor extends DefaultsProviderNoop<MyDefaultedWithoutNoArgConstructor> {

        // no no-arg constructor

        public MyDefaultedWithoutNoArgConstructor(final int value) {
        }

        @Override
        public MyDefaultedWithoutNoArgConstructor getDefaultValue() {
            return new MyDefaultedWithoutNoArgConstructor(0);
        }

    }

    public void testDefaultedMustHaveANoArgConstructor() {
        _Config.clear();
        
        facetFactory.process(new ProcessClassContext(MyDefaultedWithoutNoArgConstructor.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertNull(facet);
    }

    @Defaulted(defaultsProviderClass = MyDefaultedWithoutPublicNoArgConstructor.class)
    public static class MyDefaultedWithoutPublicNoArgConstructor extends DefaultsProviderNoop<MyDefaultedWithoutPublicNoArgConstructor> {

        // no public no-arg constructor
        MyDefaultedWithoutPublicNoArgConstructor() {
        }

        public MyDefaultedWithoutPublicNoArgConstructor(final int value) {
        }

        @Override
        public MyDefaultedWithoutPublicNoArgConstructor getDefaultValue() {
            return new MyDefaultedWithoutPublicNoArgConstructor();
        }

    }

    public void testDefaultedHaveAPublicNoArgConstructor() {
        facetFactory.process(new ProcessClassContext(MyDefaultedWithoutPublicNoArgConstructor.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertNull(facet);
    }

    @Defaulted()
    public static class MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration extends DefaultsProviderNoop<MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration> {

        /**
         * Required since is a DefaultsProvider.
         */
        public MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration getDefaultValue() {
            return new MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration();
        }
    }

    public void testDefaultedProviderNameCanBePickedUpFromConfiguration() {
        
        final String className = "org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetFactoryTest$MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration";
        
        _Config.clear();
        _Config.put(DefaultsProviderUtil.DEFAULTS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + DefaultsProviderUtil.DEFAULTS_PROVIDER_NAME_KEY_SUFFIX, className);
        facetFactory.process(new ProcessClassContext(MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertNotNull(facet);
        assertEquals(MyDefaultedWithDefaultsProviderSpecifiedUsingConfiguration.class, facet.getDefaultsProviderClass());
    }

    public static class NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration extends DefaultsProviderNoop<NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration> {

        /**
         * Required since is a DefaultsProvider.
         */
        public NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration getDefaultValue() {
            return new NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration();
        }
    }

    public void testNonAnnotatedDefaultedCanBePickedUpFromConfiguration() {
        
        final String className = "org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetFactoryTest$NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration";
        
        _Config.clear();
        _Config.put(DefaultsProviderUtil.DEFAULTS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + DefaultsProviderUtil.DEFAULTS_PROVIDER_NAME_KEY_SUFFIX, className);
        facetFactory.process(new ProcessClassContext(NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));
        final DefaultedFacetAbstract facet = (DefaultedFacetAbstract) facetedMethod.getFacet(DefaultedFacet.class);
        assertNotNull(facet);
        assertEquals(NonAnnotatedDefaultedDefaultsProviderSpecifiedUsingConfiguration.class, facet.getDefaultsProviderClass());
    }

    private String canonical(final String className) {
        return className.replace('$', '.');
    }

}
