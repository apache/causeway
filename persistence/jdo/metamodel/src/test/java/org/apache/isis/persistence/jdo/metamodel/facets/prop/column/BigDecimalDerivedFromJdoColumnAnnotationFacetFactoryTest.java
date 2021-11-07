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
package org.apache.isis.persistence.jdo.metamodel.facets.prop.column;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.isis.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;

import lombok.val;

public class BigDecimalDerivedFromJdoColumnAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    private BigDecimalInferredFromJdoColumnAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        val mmc = MetaModelContext_forTesting.buildDefault();
        facetFactory = new BigDecimalInferredFromJdoColumnAnnotationFacetFactory(mmc);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        val featureTypes = facetFactory.getFeatureTypes();
        assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER_SCALAR));
    }

    public void testAnnotationPickedUpOnProperty() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotation");
        facetFactory.process(ProcessMethodContext
                .forTesting(cls, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, 12, 3);
    }

    public void testAnnotationDefaultsLengthIfMissing() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotationMissingLength");
        facetFactory.process(ProcessMethodContext
                .forTesting(cls, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, -1, 3);
    }

    public void testAnnotationDefaultsScaleIfMissing() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotationMissingScale");
        facetFactory.process(ProcessMethodContext
                .forTesting(cls, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, 12, -1);
    }

    public void testNoFacetIfPropertyTypeIsNotBigDecimal() throws Exception {

        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getStringPropertyWithColumnAnnotation");
        facetFactory.process(ProcessMethodContext
                .forTesting(cls, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, -1, -1);
    }

    // -- HELPER

    private void assertBigDecimalSemantics(
            final FacetedMethod facetedMethod, final int maxTotalDigits, final int maxFractionalDigits) {
        if(maxTotalDigits>=0) {
            final MaxTotalDigitsFacet facet = facetedMethod.getFacet(MaxTotalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxTotalDigitsFacetInferredFromJdoColumn);
            assertThat(facet.maxTotalDigits(), is(maxTotalDigits));
        } else {
            assertNull(facetedMethod.getFacet(MaxTotalDigitsFacet.class));
        }

        if(maxFractionalDigits>=0) {
            final MaxFractionalDigitsFacet facet = facetedMethod.getFacet(MaxFractionalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxFractionDigitsFacetInferredFromJdoColumn);
            assertThat(facet.getMaxFractionalDigits(), is(maxFractionalDigits));
        } else {
            assertNull(facetedMethod.getFacet(MaxFractionalDigitsFacet.class));
        }
    }

}
