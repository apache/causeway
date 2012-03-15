package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;

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
