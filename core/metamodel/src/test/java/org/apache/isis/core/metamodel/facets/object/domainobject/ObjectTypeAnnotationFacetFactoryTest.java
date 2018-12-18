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

package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetFromObjectTypeAnnotation;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory.ProcessObjectSpecIdContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ObjectTypeAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private DomainObjectAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new DomainObjectAnnotationFacetFactory();
    }

    @Test
    public void objectTypeAnnotationPickedUpOnClass() {

        @ObjectType("CUS")
        class Customer {
        }

        expectNoMethodsRemoved();

        facetFactory.processObjectType(new ProcessObjectSpecIdContext(Customer.class, facetHolder));

        final ObjectSpecIdFacet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);

        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof ObjectSpecIdFacetFromObjectTypeAnnotation, is(true));
        assertThat(facet.value(), is(ObjectSpecId.of("CUS")));

    }

}
