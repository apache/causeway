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

package org.apache.isis.metamodel.facets.properties.propertylayout;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;

import lombok.val;

public class NamedFacetForPropertyLayoutAnnotationFactoryTest extends AbstractFacetFactoryTest {

    public void testPropertyLayoutAnnotationNamed() {
        final PropertyLayoutFacetFactory facetFactory = new PropertyLayoutFacetFactory();

        class Customer {
            @PropertyLayout(named = "1st name")
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, method,
                methodRemover, facetedMethod);

        val facetHolder = facetFactory.facetHolderFrom(processMethodContext);
        val propertyLayoutIfAny = facetFactory.propertyLayoutsFrom(processMethodContext);

        facetFactory.processNamed(facetHolder, propertyLayoutIfAny);

        // then
        final NamedFacet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NamedFacetForPropertyLayoutAnnotation.class)));
        assertThat(facet.value(), is(equalTo("1st name")));
        assertThat(facet.escaped(), is(true));
    }

    public void testPropertyLayoutAnnotationNamedEscapedFalse() {
        final PropertyLayoutFacetFactory facetFactory = new PropertyLayoutFacetFactory();

        class Customer {
            @PropertyLayout(named = "1st name", namedEscaped = false)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, null, method,
                methodRemover, facetedMethod);

        val facetHoldr = facetFactory.facetHolderFrom(processMethodContext);
        val propertyLayoutIfAny = facetFactory.propertyLayoutsFrom(processMethodContext);

        facetFactory.processNamed(facetHoldr, propertyLayoutIfAny);

        // then
        final NamedFacet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NamedFacetForPropertyLayoutAnnotation.class)));
        assertThat(facet.value(), is(equalTo("1st name")));
        assertThat(facet.escaped(), is(false));
    }

}
