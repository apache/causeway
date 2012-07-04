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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.collection.elements;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.FetchType;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.JpaFetchTypeFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.collection.elements.JpaElementCollectionAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.collection.elements.JpaElementsCollectionFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.collection.elements.JpaElementsCollectionFacetAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.collection.elements.TypeOfFacetDerivedFromJpaElementCollectionAnnotation;

public class GivenJpaElementCollectionAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaElementCollectionAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaElementCollectionAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        Assert.assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertTrue(contains(featureTypes, FeatureType.COLLECTION));
        Assert.assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testTypeOfFacetPickedUpOnCollection() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");

        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetDerivedFromJpaElementCollectionAnnotation);
    }

    public void testTypeOfFacetElementClass() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertEquals(cls, facet.value());
    }

    public void testJpaFetchTypeFacetPickedUpOnCollection() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaFetchTypeFacetAbstract);
    }

    public void testJpaFetchTypeFacetFetchType() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertEquals(FetchType.LAZY, facet.getFetchType());
    }

    public void testHibernateCollectionOfElementsFacetPickedUpOnCollection() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaElementsCollectionFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof JpaElementsCollectionFacetAnnotation);
    }

    public void testIfNoAnnotationThenNoFacets() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getOtherObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TypeOfFacet.class));
        assertNull(facetedMethod.getFacet(JpaFetchTypeFacet.class));
        assertNull(facetedMethod.getFacet(JpaElementsCollectionFacet.class));
    }

    public void testNoMethodsRemoved() throws Exception {
        final Class<?> cls = SimpleObjectWithElementCollection.class;
        final Method method = cls.getMethod("getObjects");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

}
