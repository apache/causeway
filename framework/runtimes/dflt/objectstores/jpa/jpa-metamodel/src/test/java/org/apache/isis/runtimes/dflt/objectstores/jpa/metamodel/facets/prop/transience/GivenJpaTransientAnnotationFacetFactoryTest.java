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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience.DerivedFacetDerivedFromJpaTransientAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience.JpaTransientAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience.JpaTransientFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience.JpaTransientFacetAnnotation;

public class GivenJpaTransientAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaTransientAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaTransientAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        Assert.assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        Assert.assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testTransientAnnotationPickedUpOnProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithTransientAnnotation.class;
        final Method method = cls.getMethod("getTransientColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaTransientFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaTransientFacetAnnotation);
    }

    public void testDerivedFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithTransientAnnotation.class;
        final Method method = cls.getMethod("getTransientColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final NotPersistedFacet facet = facetedMethod.getFacet(NotPersistedFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof DerivedFacetDerivedFromJpaTransientAnnotation);
    }

    public void testIfNoTransientAnnotationThenNoManyToOneColumnFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoTransientAnnotation.class;
        final Method method = cls.getMethod("getTransientColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(JpaTransientFacet.class));
    }

    public void testIfNoTransientAnnotationThenNoDerivedFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoTransientAnnotation.class;
        final Method method = cls.getMethod("getTransientColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(NotPersistedFacet.class));
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithTransientAnnotation.class;
        final Method method = cls.getMethod("getTransientColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }
}
