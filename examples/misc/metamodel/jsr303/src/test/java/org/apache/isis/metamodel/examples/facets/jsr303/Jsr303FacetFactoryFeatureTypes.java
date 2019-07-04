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


package org.apache.isis.metamodel.examples.facets.jsr303;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.metamodel.examples.facets.jsr303.Jsr303FacetFactory;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;



public class Jsr303FacetFactoryFeatureTypes {

    private Jsr303FacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new Jsr303FacetFactory();
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    @Test
    public void featureTypesLength() {
        ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertThat(featureTypes.length, is(2));
    }

    @Test
    public void featureTypesContainsTypeRepresentingObject() {
        ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertThat(featureTypes, hasItemInArray(ObjectFeatureType.OBJECT));
    }

    @Test
    public void featureTypesContainsTypeRepresentingProperty() {
        ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertThat(featureTypes, hasItemInArray(ObjectFeatureType.PROPERTY));
    }


}
