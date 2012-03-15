package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.FetchType;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;

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

// Copyright (c) Naked Objects Group Ltd.
