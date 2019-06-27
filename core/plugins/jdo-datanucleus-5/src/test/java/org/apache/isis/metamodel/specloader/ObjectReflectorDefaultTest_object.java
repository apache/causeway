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

package org.apache.isis.metamodel.specloader;

import org.datanucleus.enhancement.Persistable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacet;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.SpecificationLoaderTestAbstract;

class ObjectReflectorDefaultTest_object extends SpecificationLoaderTestAbstract {

    public abstract static class TestDomainObject implements Persistable {
    }

    @Override
    protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
        return reflector.loadSpecification(TestDomainObject.class);
    }

    @Test
    void testType() throws Exception {
        assertTrue(specification.isNotCollection());
    }

    @Test
    void testName() throws Exception {
        assertEquals(TestDomainObject.class.getName(), specification.getFullIdentifier());
    }

    @Test
    void testStandardFacets() throws Exception {
        assertNotNull(specification.getFacet(NamedFacet.class));
        assertNotNull(specification.getFacet(DescribedAsFacet.class));
        assertNotNull(specification.getFacet(TitleFacet.class));
        assertNotNull(specification.getFacet(PluralFacet.class));
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
