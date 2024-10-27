/*
 *  Licensed to the Apache Software Foundation (ASF) under one
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
 *  under the License.
 */
package org.apache.causeway.core.metamodel.facets.object.domainobjectlayout;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

class DomainObjectLayoutFactoryTest
extends FacetFactoryTestAbstract {

    DomainObjectLayoutFacetFactory facetFactory;

    // -- TEST LIFE CYCLING

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new DomainObjectLayoutFacetFactory(getMetaModelContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    // -- DOMAIN OBJECTS FOR TESTING

    @DomainObjectLayout(
            bookmarking = BookmarkPolicy.AS_ROOT,
            cssClass = "foobar",
            cssClassFa = "foo",
            cssClassFaPosition = CssClassFaPosition.RIGHT,
            describedAs = "This is a description",
            named = "Name override",
            paged = 20
            )
    class Customer { }

    @DomainObjectLayout
    class CustomerWithDefaults { }

    // -- LAYOUT TESTS

    public static class DomainObjectLayout_bookmarking extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(BookmarkPolicyFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof BookmarkPolicyFacetForDomainObjectLayoutAnnotation);

                final BookmarkPolicyFacetForDomainObjectLayoutAnnotation facetImpl =
                        (BookmarkPolicyFacetForDomainObjectLayoutAnnotation) facet;

                assertThat(facetImpl.value(), is(BookmarkPolicy.AS_ROOT));

                assertNoMethodsRemoved();

            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final BookmarkPolicyFacet facet = facetHolder.getFacet(BookmarkPolicyFacet.class);
                assertThat(facet.value(), is(BookmarkPolicy.NOT_SPECIFIED));

                assertNoMethodsRemoved();
            });
        }

    }
    // --

    public static class DomainObjectLayout_cssClass extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(CssClassFacet.class);
                System.err.printf("%s%n", facet);
                assertNotNull(facet);
                assertTrue(facet instanceof CssClassFacetForDomainObjectLayoutAnnotation);

                var facetImpl =
                        (CssClassFacetForDomainObjectLayoutAnnotation) facet;
                var mockAdapter = Mockito.mock(ManagedObject.class);
                assertThat(facetImpl.cssClass(mockAdapter), is("foobar"));

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(CssClassFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }
    }

    public static class DomainObjectLayout_cssClassFa extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(FaFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof FaFacetForDomainObjectLayoutAnnotation);

                final FaFacetForDomainObjectLayoutAnnotation facetImpl = (FaFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.getLayers().getIconEntries().get(0).getCssClasses(), equalTo("fa fa-foo fa-fw"));
                assertThat(facetImpl.getLayers().getPosition(), is(CssClassFaPosition.RIGHT));

                assertNoMethodsRemoved();

            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(FaFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }
    }

    public static class DomainObjectLayout_describedAs extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ObjectDescribedFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof ObjectDescribedFacetForDomainObjectLayoutAnnotation);

                final ObjectDescribedFacetForDomainObjectLayoutAnnotation facetImpl = (ObjectDescribedFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.text(), is("This is a description"));

                assertNoMethodsRemoved();

            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ObjectDescribedFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }
    }

    public static class DomainObjectLayout_named extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                var namedFacet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNotNull(namedFacet);
                assertTrue(namedFacet instanceof ObjectNamedFacetForDomainObjectLayoutAnnotation);

                assertEquals("Name override", namedFacet.singular());

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                var facet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }
    }

    public static class DomainObjectLayout_paged extends DomainObjectLayoutFactoryTest {

        @Test
        public void whenSpecified() {
            objectScenario(DomainObjectLayoutFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(PagedFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof PagedFacetForDomainObjectLayoutAnnotation);

                final PagedFacetForDomainObjectLayoutAnnotation facetImpl = (PagedFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.value(), is(20));

                assertNoMethodsRemoved();

            });
        }

        @Test
        public void whenDefaults() {
            objectScenario(CustomerWithDefaults.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(PagedFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }
    }
}
