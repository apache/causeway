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
package org.apache.isis.core.metamodel.facets.object.value;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory;

public class ValueFacetAnnotationOrConfigurationFactoryTest extends AbstractFacetFactoryTest {

    private ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testValueSemanticsProviderMustBeAValueSemanticsProvider() {
        // no test, because compiler prevents us from nominating a class that
        // doesn't
        // implement ValueSemanticsProvider
    }


    @Value()
    public static class MyValueWithSemanticsProviderSpecifiedUsingConfiguration extends AbstractValueSemanticsProvider<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> implements Parser<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> {

        /**
         * Required since is a SemanticsProvider.
         */
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration parseTextRepresentation(
                final ValueSemanticsProvider.Context context,
                final String entry) {
            return null;
        }

        @Override
        public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final MyValueWithSemanticsProviderSpecifiedUsingConfiguration existing) {
            return null;
        }

        @Override
        public int typicalLength() {
            return 0;
        }
    }

    public static class NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration extends AbstractValueSemanticsProvider<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> implements Parser<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> {
        /**
         * Required since is a SemanticsProvider.
         */
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration parseTextRepresentation(
                final ValueSemanticsProvider.Context context,
                final String entry) {
            return null;
        }

        @Override
        public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration existing) {
            return null;
        }

        @Override
        public int typicalLength() {
            return 0;
        }
    }

}
