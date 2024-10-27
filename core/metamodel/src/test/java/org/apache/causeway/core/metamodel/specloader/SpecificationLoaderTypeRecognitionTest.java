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
package org.apache.causeway.core.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;

import lombok.Getter;

class SpecificationLoaderTypeRecognitionTest
implements HasMetaModelContext {

    @Getter
    private MetaModelContext metaModelContext;

    @BeforeEach
    final void setUp() throws Exception {
        metaModelContext = MetaModelContext_forTesting.buildDefault();
    }

    @AfterEach
    final void tearDown() throws Exception {
        metaModelContext.getSpecificationLoader().disposeMetaModel();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            int.class,
            String.class
    })
    void singularType(final Class<?> typeUnderTest) {
        // given
        var specification = getSpecificationLoader().loadSpecification(typeUnderTest);
        // then
        assertTrue(specification.isSingular());
        assertEquals(typeUnderTest.getName(), specification.getFullIdentifier());

        final Facet collectionFacet = specification.getFacet(CollectionFacet.class);
        assertNull(collectionFacet);

        final TypeOfFacet typeOfFacet = specification.getFacet(TypeOfFacet.class);
        assertNull(typeOfFacet);

        var namedFacet = specification.lookupFacet(ObjectNamedFacet.class)
                .map(Facet.class::cast)
                .or(()->specification.lookupFacet(MemberNamedFacet.class))
                .orElse(null);
        assertNotNull(namedFacet);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Set.class,
            List.class,
            Collection.class,
            Vector.class,
            Can.class,
            TestPojo[].class
    })
    void pluralType(final Class<?> typeUnderTest) {
        // given
        var specification = getSpecificationLoader().loadSpecification(typeUnderTest);

        // then
        assertTrue(specification.isPlural());

        var containerType = typeUnderTest.isArray()
                ? java.lang.reflect.Array.class
                : typeUnderTest;

        assertEquals(containerType.getName(), specification.getFullIdentifier());

        var collectionFacet = specification.getFacet(CollectionFacet.class);
        assertNotNull(collectionFacet);

        var typeOfFacet = specification.getFacet(TypeOfFacet.class);
        assertNotNull(typeOfFacet);
        assertEquals(Optional.of(containerType), typeOfFacet.value().containerType());
        assertEquals(Object.class, typeOfFacet.value().elementType());

        var namedFacet = specification.lookupFacet(ObjectNamedFacet.class)
                .map(Facet.class::cast)
                .or(()->specification.lookupFacet(MemberNamedFacet.class))
                .orElse(null);
        assertNotNull(namedFacet);
    }

}
