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
package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CssClassFa;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop.CssClassFaFacetOnMemberFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CssClassFaAnnotationOnMemberFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    @Test
    public void testCssClassFaAnnotationPickedUpOnClass() {

        final CssClassFaFacetOnMemberFactory facetFactory = new CssClassFaFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {

            @CssClassFa(value = "fa fa-foo")
            public String foo() {
                return "Joe";
            }
        }

        expectNoMethodsRemoved();

        final Method actionMethod = findMethod(Customer.class, "foo", new Class[] { });

        facetedMethod = FacetedMethod.createForAction(Customer.class, actionMethod);
        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet, is(instanceOf(CssClassFaFacetAbstract.class)));
        final CssClassFaFacetAbstract cssClassFacetAbstract = (CssClassFaFacetAbstract) facet;
        assertThat(cssClassFacetAbstract.value(), equalTo("fa fa-fw fa-foo"));
        assertThat(cssClassFacetAbstract.getPosition(), is(ActionLayout.CssClassFaPosition.LEFT));
    }

    @Test
    public void testCssClassFaAnnotationPickedUpOnClassRightPosition() {

        final CssClassFaFacetOnMemberFactory facetFactory = new CssClassFaFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {

            @CssClassFa(value = "fa fa-foo", position = ActionLayout.CssClassFaPosition.RIGHT)
            public String foo() {
                return "Joe";
            }
        }

        expectNoMethodsRemoved();

        final Method actionMethod = findMethod(Customer.class, "foo", new Class[] { });

        facetedMethod = FacetedMethod.createForAction(Customer.class, actionMethod);
        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet, is(instanceOf(CssClassFaFacetAbstract.class)));
        final CssClassFaFacetAbstract cssClassFacetAbstract = (CssClassFaFacetAbstract) facet;
        assertThat(cssClassFacetAbstract.value(), equalTo("fa fa-fw fa-foo"));
        assertThat(cssClassFacetAbstract.getPosition(), is(ActionLayout.CssClassFaPosition.RIGHT));
    }
}
