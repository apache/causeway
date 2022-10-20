/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.object.cssclassfa;

import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;

public class CssClassFaFacetOnTypeAnnotationFactoryTest extends AbstractFacetFactoryJupiterTestCase {

    // @Test
    // public void testCssClassFaAnnotationPickedUpOnClass() {
    //
    // final CssClassFaFacetOnTypeAnnotationFactory facetFactory = new CssClassFaFacetOnTypeAnnotationFactory();
    // facetFactory.setSpecificationLoader(mockSpecificationLoader);
    //
    // @CssClassFa("fa-foo")
    // class Customer {
    // }
    //
    // expectNoMethodsRemoved();
    //
    // facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, null, mockMethodRemover,
    // facetedMethod));
    //
    // final Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
    // assertThat(facet, is(not(nullValue())));
    // assertThat(facet, is(instanceOf(CssClassFaFacetAbstract.class)));
    // final CssClassFaFacetAbstract cssClassFacetAbstract = (CssClassFaFacetAbstract) facet;
    // assertThat(cssClassFacetAbstract.value(), equalTo("fa fa-fw fa-foo"));
    // assertThat(cssClassFacetAbstract.getPosition(), equalTo(ActionLayout.CssClassFaPosition.LEFT));
    // }

    // @Test
    // public void testCssClassFaAnnotationPickedUpOnClassPositionRight() {
    //
    // final CssClassFaFacetOnTypeAnnotationFactory facetFactory = new CssClassFaFacetOnTypeAnnotationFactory();
    // facetFactory.setSpecificationLoader(mockSpecificationLoader);
    //
    // @CssClassFa(value = "fa-foo", position = ActionLayout.CssClassFaPosition.RIGHT)
    // class Customer {
    // }
    //
    // expectNoMethodsRemoved();
    //
    // facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, null, mockMethodRemover,
    // facetedMethod));
    //
    // final Facet facet = facetedMethod.getFacet(CssClassFaFacet.class);
    // assertThat(facet, is(not(nullValue())));
    // assertThat(facet, is(instanceOf(CssClassFaFacetAbstract.class)));
    // final CssClassFaFacetAbstract cssClassFacetAbstract = (CssClassFaFacetAbstract) facet;
    // assertThat(cssClassFacetAbstract.value(), equalTo("fa fa-fw fa-foo"));
    // assertThat(cssClassFacetAbstract.getPosition(), equalTo(ActionLayout.CssClassFaPosition.RIGHT));
    // }
}
