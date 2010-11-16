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


package org.apache.isis.defaults.progmodel;

import org.junit.Assert;
import org.junit.Test;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.progmodel.facets.object.ident.plural.PluralFacet;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.progmodel.facets.object.validprops.ObjectValidPropertiesFacet;
import org.apache.isis.core.runtime.system.TestDomainObject;
import org.apache.isis.defaults.progmodel.JavaReflector;



public class JavaReflector_ObjectTest extends JavaReflectorTestAbstract {

    @Override
    protected ObjectSpecification loadSpecification(final JavaReflector reflector) {
        return reflector.loadSpecification(TestDomainObject.class);
    }

    @Test
    public void testType() throws Exception {
        Assert.assertTrue(specification.isNotCollection());
    }

    @Test
    public void testName() throws Exception {
        Assert.assertEquals(TestDomainObject.class.getName(), specification.getFullName());
    }

    @Test
    public void testStandardFacets() throws Exception {
        Assert.assertNotNull(specification.getFacet(NamedFacet.class));
        Assert.assertNotNull(specification.getFacet(DescribedAsFacet.class));
        Assert.assertNotNull(specification.getFacet(TitleFacet.class));
        Assert.assertNotNull(specification.getFacet(PluralFacet.class));
        Assert.assertNotNull(specification.getFacet(NotPersistableFacet.class));
        Assert.assertNotNull(specification.getFacet(ObjectValidPropertiesFacet.class));
    }

    @Test
    public void testNoCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void testNoTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

}

