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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory.ProcessObjectTypeContext;
import org.apache.isis.core.metamodel.facets.object.domainobject.logicaltype.LogicalTypeFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;

import lombok.val;

public class ObjectTypeAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private DomainObjectAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new DomainObjectAnnotationFacetFactory(metaModelContext, new MethodByClassMap());
    }

    @Test
    public void logicalTypeNameAnnotationPickedUpOnClass() {

        @DomainObject(logicalTypeName = "CUS")
        class Customer {
        }

        expectNoMethodsRemoved();

        val context = new ProcessObjectTypeContext(Customer.class, facetHolder);
        facetFactory.processLogicalTypeName(context.synthesizeOnType(DomainObject.class), context);

        final LogicalTypeFacet facet = facetHolder.getFacet(LogicalTypeFacet.class);

        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof LogicalTypeFacetForDomainObjectAnnotation, is(true));
        assertThat(facet.value(), is("CUS"));

    }

}
