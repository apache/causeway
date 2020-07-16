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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.persistence.jdo.datanucleus5.testing.AbstractFacetFactoryTest;

import lombok.val;

public class MandatoryDerivedFromJdoColumnAnnotationFacetFactoryTest 
extends AbstractFacetFactoryTest {

    private MandatoryFromJdoColumnAnnotationFacetFactory facetFactory;
    private Class<?> cls;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new MandatoryFromJdoColumnAnnotationFacetFactory();
        cls = SimpleObjectWithColumnAllowsNullAnnotations.class;
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

    public void testPrimitiveWithNoAnnotation_isMandatory() throws Exception {
        final Method method = cls.getMethod("getPrimitiveWithNoAnnotation");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetInferredFromAbsenceOfJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(false));
    }

    public void testPrimitiveWithNoAllowsNull_isMandatory() throws Exception {
        final Method method = cls.getMethod("getPrimitiveWithNoAllowsNull");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(false));
    }

    public void testPrimitiveWithAllowsNullFalse() throws Exception {
        final Method method = cls.getMethod("getPrimitiveWithAllowsNullFalse");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(false));
    }

    public void testPrimitiveWithAllowsNullTrue() throws Exception {
        final Method method = cls.getMethod("getPrimitiveWithAllowsNullTrue");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(true));
    }

    public void testReferenceWithNoAnnotation_isOptional() throws Exception {
        final Method method = cls.getMethod("getReferenceWithNoAnnotation");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetInferredFromAbsenceOfJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(true));
    }

    public void testReferenceWithNoAllowsNull_isOptional() throws Exception {
        final Method method = cls.getMethod("getReferenceWithNoAllowsNull");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(true));
    }

    public void testReferenceWithAllowsNullFalse() throws Exception {
        final Method method = cls.getMethod("getReferenceWithAllowsNullFalse");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(false));
    }

    public void testReferenceWithAllowsNullTrue() throws Exception {
        final Method method = cls.getMethod("getReferenceWithAllowsNullTrue");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MandatoryFacetDerivedFromJdoColumn);
        assertThat(facet.isInvertedSemantics(), is(true));
    }

}
