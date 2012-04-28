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
package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.aggregated.ParentedFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class GivenJpaEmbeddableAnnotationFacetFactoryTest extends
        AbstractFacetFactoryTest {

    private JpaEmbeddableAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaEmbeddableAnnotationFacetFactory();
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


    public void testAggregatedFacetPickedUpOnType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(ParentedFacet.class);
        assertNotNull(facet);
        Assert
                .assertTrue(facet instanceof ParentedFacetDerivedFromJpaEmbeddableAnnotation);
    }

    public void testJpaEmbeddableFacetPickedUpOnType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JpaEmbeddableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaEmbeddableFacetAnnotation);
    }


    public void testNoMethodsRemovedForType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));
        assertNoMethodsRemoved();
    }
}
