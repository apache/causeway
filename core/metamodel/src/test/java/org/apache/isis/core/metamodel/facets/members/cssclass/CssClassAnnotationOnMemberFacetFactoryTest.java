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
package org.apache.isis.core.metamodel.facets.members.cssclass;

import org.junit.Test;
import org.apache.isis.applib.annotation.CssClass;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnMemberFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CssClassAnnotationOnMemberFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    @Test
    public void testCssClassAnnotationPickedUpOnProperty() {

        final CssClassFacetOnMemberFactory facetFactory = new CssClassFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {

            @CssClass(value = "user")
            public String getName() {
                return "Joe";
            }
        }

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "name");
        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof CssClassFacetAbstract, is(true));
        final CssClassFacetAbstract cssClassFacetAbstract = (CssClassFacetAbstract) facet;
        assertThat(cssClassFacetAbstract.cssClass(null), equalTo("user"));
    }
}
