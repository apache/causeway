package org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.objecttype.ObjectTypeFacet;


public class GivenJpaDiscriminatorValueAnnotationFacetFactoryTest extends
        AbstractFacetFactoryTest {

    private JpaDiscriminatorValueAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaDiscriminatorValueAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory
                .getFeatureTypes();
        Assert
                .assertTrue(contains(featureTypes,
                FeatureType.OBJECT));
        assertFalse(contains(featureTypes,
                FeatureType.PROPERTY));
        assertFalse(contains(featureTypes,
                FeatureType.COLLECTION));
        Assert
                .assertFalse(contains(featureTypes,
                FeatureType.ACTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION_PARAMETER));
    }

    public void testDiscriminatorValueAnnotationPickedUpOnClass() {
        @DiscriminatorValue("CUS")
        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder
                .getFacet(ObjectTypeFacet.class);
        assertNotNull(facet);
        Assert
                .assertTrue(facet instanceof ObjectTypeFacetInferredFromJpaDiscriminatorValueAnnotation);
    }

    public void testIfNoEntityAnnotationThenNoFacet() {

        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder
                .getFacet(ObjectTypeFacet.class);
        assertNull(facet);
    }

    public void testAnnotationValue() {
        @DiscriminatorValue("CUS")
        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final ObjectTypeFacet discriminatorValueFacet = facetHolder
                .getFacet(ObjectTypeFacet.class);
        assertEquals("CUS", discriminatorValueFacet.value());
    }

    public void testNoMethodsRemoved() {
        @Entity
        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        assertNoMethodsRemoved();
    }


}

// Copyright (c) Naked Objects Group Ltd.
