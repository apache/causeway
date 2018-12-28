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

import org.datanucleus.enhancement.Persistable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ObjectReflectorDefaultTest_object extends SpecificationLoaderTestAbstract {

    abstract static class TestDomainObject implements Persistable {
    }

    @Override
    protected ObjectSpecification loadSpecification(final SpecificationLoader reflector) {
        return reflector.loadSpecification(TestDomainObject.class);
    }

    @Ignore("broken")
    @Test
    public void testType() throws Exception {
        Assert.assertTrue(specification.isNotCollection());
    }

    @Ignore("broken")
    @Test
    public void testName() throws Exception {
        Assert.assertEquals(TestDomainObject.class.getName(), specification.getFullIdentifier());
    }

    @Ignore("broken")
    @Test
    public void testStandardFacets() throws Exception {
        Assert.assertNotNull(specification.getFacet(NamedFacet.class));
        Assert.assertNotNull(specification.getFacet(DescribedAsFacet.class));
        Assert.assertNotNull(specification.getFacet(TitleFacet.class));
        Assert.assertNotNull(specification.getFacet(PluralFacet.class));
        Assert.assertNotNull(specification.getFacet(NotPersistableFacet.class));
        Assert.assertNotNull(specification.getFacet(ObjectValidPropertiesFacet.class));
    }

    @Ignore("broken")
    @Test
    public void testNoCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        Assert.assertNull(facet);
    }

    @Ignore("broken")
    @Test
    public void testNoTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

}
