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
package org.apache.isis.core.metamodel.specloader;

import java.util.Optional;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

class SpecificationLoaderTest_nonScalar {

    class ArrayTest extends SpecificationLoaderTestAbstract {

        @Override
        protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
            return reflector.loadSpecification(ReflectorTestPojo[].class);
        }

        @Test void testType() throws Exception {
            assertTrue(specification.isNonScalar());
        }

        @Test void testName() throws Exception {
            assertEquals(ReflectorTestPojo[].class.getName(), specification.getFullIdentifier());
        }

        @Test @Override public void testCollectionFacet() throws Exception {
            final Facet facet = specification.getFacet(CollectionFacet.class);
            assertNotNull(facet);
        }

        @Test @Override public void testTypeOfFacet() throws Exception {
            final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertEquals(Optional.of(ReflectorTestPojo[].class), facet.value().getContainerType());
            assertEquals(ReflectorTestPojo.class, facet.value().getElementType());
        }

    }


    static class CanTest extends SpecificationLoaderTestAbstract {

        @Override
        protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
            return reflector.loadSpecification(Can.class);
        }

        @Test void testType() throws Exception {
            assertTrue(specification.isNonScalar());
        }

        @Test void testName() throws Exception {
            assertEquals(Can.class.getName(), specification.getFullIdentifier());
        }

        @Test @Override public void testCollectionFacet() throws Exception {
            final Facet facet = specification.getFacet(CollectionFacet.class);
            assertNotNull(facet);
        }

        @Test @Override public void testTypeOfFacet() throws Exception {
            final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertEquals(Optional.of(Can.class), facet.value().getContainerType());
            assertEquals(Object.class, facet.value().getElementType());
        }

    }

    static class VectorTest extends SpecificationLoaderTestAbstract {

        @Override
        protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
            return reflector.loadSpecification(Vector.class);
        }

        @Test void testType() throws Exception {
            assertTrue(specification.isNonScalar());
        }

        @Test void testName() throws Exception {
            assertEquals(Vector.class.getName(), specification.getFullIdentifier());
        }

        @Test @Override public void testCollectionFacet() throws Exception {
            final Facet facet = specification.getFacet(CollectionFacet.class);
            assertNotNull(facet);
        }

        @Test @Override public void testTypeOfFacet() throws Exception {
            final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
            assertNotNull(facet);
            assertEquals(Optional.of(Vector.class), facet.value().getContainerType());
            assertEquals(Object.class, facet.value().getElementType());
        }

    }

}
