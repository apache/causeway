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

package org.apache.isis.core.metamodel.facets.object.ident.cssclass;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethod;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethodFactory;

public class CssClassFacetMethodFactoryTest extends AbstractFacetFactoryTest {

    private CssClassFacetMethodFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new CssClassFacetMethodFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testIconNameMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @SuppressWarnings("unused")
            public String cssClass() {
                return null;
            }
        }
        final Method cssClassNameMethod = findMethod(Customer.class, "cssClass");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(CssClassFacetMethod.class)));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(cssClassNameMethod));
    }

}
