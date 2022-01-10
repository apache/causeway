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
package org.apache.isis.core.metamodel.facets.object.logicaltype;

import org.datanucleus.testing.dom.CustomerAsProxiedByDataNucleus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.logicaltype.classname.LogicalTypeFacetFromClassName;
import org.apache.isis.core.metamodel.facets.object.logicaltype.classname.LogicalTypeFacetFromClassNameFactory;
import org.apache.isis.core.metamodel.facets.value.annotation.LogicalTypeFacetForValueAnnotation;
import org.apache.isis.core.metamodel.facets.value.annotation.ValueAnnotationFacetFactory;

public class LogicalTypeFacetFromClassNameFactoryTest
extends AbstractFacetFactoryJUnit4TestCase {

    private ObjectTypeFacetFactory facetFactory;

    public static class Customer {
    }

    @Test
    public void installsFacet_passedThroughClassSubstitutor() {

        facetFactory = LogicalTypeFacetFromClassNameFactory.forTesting(metaModelContext);

        expectNoMethodsRemoved();

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(CustomerAsProxiedByDataNucleus.class, facetHolder));

        final LogicalTypeFacet facet = facetHolder.getFacet(LogicalTypeFacet.class);

        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof LogicalTypeFacetFromClassName, is(true));
        assertThat(facet.value(), is(Customer.class.getCanonicalName()));
    }

    @Value
    public static class ValueExample1 {
    }

    @Test
    public void installsFacet_onValues() {

        facetFactory = LogicalTypeFacetFromClassNameFactory.forTesting(metaModelContext);

        expectNoMethodsRemoved();

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(ValueExample1.class, facetHolder));

        final LogicalTypeFacet facet = facetHolder.getFacet(LogicalTypeFacet.class);

        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof LogicalTypeFacetFromClassName, is(true));
        assertThat(facet.value(), is(ValueExample1.class.getCanonicalName()));
    }

    @Value(logicalTypeName = "xxx.ValueExample")
    public static class ValueExample2 {
    }

    @Test
    public void installsFacet_onValuesUsingLogicalTypeName() {

        facetFactory = new ValueAnnotationFacetFactory(metaModelContext);

        expectNoMethodsRemoved();

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(ValueExample2.class, facetHolder));

        final LogicalTypeFacet facet = facetHolder.getFacet(LogicalTypeFacet.class);

        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof LogicalTypeFacetForValueAnnotation, is(true));
        assertThat(facet.value(), is("xxx.ValueExample"));
    }



}

