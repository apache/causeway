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
package org.apache.isis.persistence.jdo.metamodel.specloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

class ObjectReflectorDefaultTest_object extends SpecificationLoaderTestAbstract {

    public abstract static class TestDomainObject {
    }

    @Override
    protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
        return reflector.specForType(TestDomainObject.class).orElse(null);
    }

    @Test
    void testType() throws Exception {
        assertTrue(specification.isScalar());
    }

    @Test
    void testName() throws Exception {
        assertEquals(TestDomainObject.class.getName(), specification.getFullIdentifier());
    }

    @Test
    void testStandardFacets() throws Exception {

        assertNotNull(

                specification.lookupFacet(ObjectNamedFacet.class)
                .map(Facet.class::cast)
                .or(()->specification.lookupFacet(MemberNamedFacet.class))
                .orElse(null));

        //assertNotNull(specification.getFacet(ObjectDescribedFacet.class));
        assertNotNull(specification.getFacet(TitleFacet.class));
        assertNotNull(specification.getFacet(ObjectValidPropertiesFacet.class));
    }

    @Test
    void testNoCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        assertNull(facet);
    }

    @Test
    void testNoTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        assertNull(facet);
    }

}
