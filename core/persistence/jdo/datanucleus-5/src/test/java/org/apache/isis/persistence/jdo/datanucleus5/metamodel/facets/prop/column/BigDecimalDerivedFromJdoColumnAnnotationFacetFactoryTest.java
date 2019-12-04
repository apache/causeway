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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.column;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BigDecimalDerivedFromJdoColumnAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private BigDecimalDerivedFromJdoColumnAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new BigDecimalDerivedFromJdoColumnAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER_SCALAR));
    }

    public void testAnnotationPickedUpOnProperty() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotation");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final BigDecimalValueFacet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetDerivedFromJdoColumn);
        assertThat(facet.getPrecision(), is(12));
        assertThat(facet.getScale(), is(3));
    }

    public void testAnnotationDefaultsLengthIfMissing() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotationMissingLength");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final BigDecimalValueFacet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetDerivedFromJdoColumn);
        assertThat(facet.getPrecision(), is(18));
    }

    public void testAnnotationDefaultsScaleIfMissing() throws Exception {
        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithColumnAnnotationMissingScale");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final BigDecimalValueFacet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetDerivedFromJdoColumn);
        assertThat(facet.getScale(), is(2));
    }

    public void testNoFacetIfPropertyTypeIsNotBigDecimal() throws Exception {

        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getStringPropertyWithColumnAnnotation");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNull(facet);
    }

    public void testFallbackFacetIfPropertyIsNotAnnotated() throws Exception {

        final Class<?> cls = SimpleObjectWithBigDecimalColumnAnnotations.class;
        final Method method = cls.getMethod("getBigDecimalPropertyWithoutColumnAnnotation");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(BigDecimalValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof BigDecimalFacetFallback);
    }
}
