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

package org.apache.isis.metamodel.facets.object.objectspecid;

import org.datanucleus.testing.dom.CustomerAsProxiedByDataNucleus;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassName;
import org.apache.isis.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.metamodel.spec.ObjectSpecId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ObjectSpecIdFacetDerivedFromClassNameFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private ObjectSpecIdFacetDerivedFromClassNameFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new ObjectSpecIdFacetDerivedFromClassNameFactory();
    }

    public static class Customer {
    }

    @Test
    public void installsFacet_passedThroughClassSubstitutor() {
        expectNoMethodsRemoved();

        facetFactory.process(new ObjectSpecIdFacetFactory.ProcessObjectSpecIdContext(CustomerAsProxiedByDataNucleus.class, facetHolder));

        final ObjectSpecIdFacet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
        
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof ObjectSpecIdFacetDerivedFromClassName, is(true));
        assertThat(facet.value(), is(ObjectSpecId.of(Customer.class.getCanonicalName())));
    }

}

