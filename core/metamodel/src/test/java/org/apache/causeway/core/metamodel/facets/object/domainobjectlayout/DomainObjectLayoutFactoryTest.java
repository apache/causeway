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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.val;

@ExtendWith(MockitoExtension.class)
class DomainObjectLayoutFactoryTest
extends AbstractFacetFactoryJupiterTestCase {

    DomainObjectLayoutFacetFactory facetFactory;

    // -- TEST LIFE CYCLING

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new DomainObjectLayoutFacetFactory(metaModelContext);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    // -- DOMAIN OBJECTS FOR TESTING

    @DomainObjectLayout(
            bookmarking = BookmarkPolicy.AS_ROOT,
            cssClass = "foobar",
            cssClassFa = "foo",
            cssClassFaPosition = CssClassFaPosition.RIGHT,
            describedAs = "This is a description",
            named = "Name override",
            paged = 20,
            plural = "Customers Plural Form"
            )
    class Customer { }

    @DomainObjectLayout
    class CustomerWithDefaults { }

    // -- LAYOUT TESTS

    public static class Bookmarking extends DomainObjectLayoutFactoryTest {

        public static class ForDomainObjectLayout extends Bookmarking {

            @BeforeEach
            public void setUp2() throws Exception {

            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(BookmarkPolicyFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof BookmarkPolicyFacetForDomainObjectLayoutAnnotation);

                final BookmarkPolicyFacetForDomainObjectLayoutAnnotation facetImpl =
                        (BookmarkPolicyFacetForDomainObjectLayoutAnnotation) facet;

                assertThat(facetImpl.value(), is(BookmarkPolicy.AS_ROOT));

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final BookmarkPolicyFacet facet = facetHolder.getFacet(BookmarkPolicyFacet.class);
                assertThat(facet.value(), is(BookmarkPolicy.NOT_SPECIFIED));

                expectNoMethodsRemoved();
            }
        }

    }
    // --

    public static class CssClass extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends CssClass {

            @Override
            @BeforeEach
            public void setUp() throws Exception {
                super.setUp();
            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(CssClassFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof CssClassFacetForDomainObjectLayoutAnnotation);

                final CssClassFacetForDomainObjectLayoutAnnotation facetImpl =
                        (CssClassFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.cssClass(mockAdapter), is("foobar"));

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(CssClassFacet.class);
                assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

    }

    public static class CssClassFa extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends CssClassFa {

            @BeforeEach
            public void setUp2() throws Exception {
            }


            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(CssClassFaFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof CssClassFaFacetForDomainObjectLayoutAnnotation);

                final CssClassFaFacetForDomainObjectLayoutAnnotation facetImpl = (CssClassFaFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.asSpaceSeparated(), equalTo("fa fa-fw fa-foo"));
                assertThat(facetImpl.getPosition(), is(CssClassFaPosition.RIGHT));

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(CssClassFaFacet.class);
                assertNull(facet);

                expectNoMethodsRemoved();
            }
        }


    }

    public static class DescribedAs extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends DescribedAs {

            @BeforeEach
            public void setUp2() throws Exception {
            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ObjectDescribedFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof ObjectDescribedFacetForDomainObjectLayoutAnnotation);

                final ObjectDescribedFacetForDomainObjectLayoutAnnotation facetImpl = (ObjectDescribedFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.text(), is("This is a description"));

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ObjectDescribedFacet.class);
                assertNull(facet);

                expectNoMethodsRemoved();
            }
        }


    }

    public static class Named extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends Named {

            @BeforeEach
            public void setUp2() throws Exception {
            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                val namedFacet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNotNull(namedFacet);
                assertTrue(namedFacet instanceof ObjectNamedFacetForDomainObjectLayoutAnnotation);

                assertEquals("Name override", namedFacet.singular());

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                val facet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNull(facet);

                expectNoMethodsRemoved();
            }
        }


    }

    public static class Paged extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends Paged {

            @BeforeEach
            public void setUp2() throws Exception {
            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PagedFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof PagedFacetForDomainObjectLayoutAnnotation);

                final PagedFacetForDomainObjectLayoutAnnotation facetImpl = (PagedFacetForDomainObjectLayoutAnnotation) facet;
                assertThat(facetImpl.value(), is(20));

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls,mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PagedFacet.class);
                assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

    }

    public static class Plural extends DomainObjectLayoutFactoryTest {

        @Mock ManagedObject mockAdapter;

        public static class ForDomainObjectLayout extends Plural {

            @BeforeEach
            public void setUp2() throws Exception {
            }

            @Test
            public void whenSpecified() {

                final Class<?> cls = DomainObjectLayoutFactoryTest.Customer.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                val namedFacet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNotNull(namedFacet);

                assertEquals("Customers Plural Form", namedFacet.pluralTranslated());

                expectNoMethodsRemoved();
            }

            @Test
            public void whenDefaults() {

                final Class<?> cls = CustomerWithDefaults.class;

                facetFactory.process(ProcessClassContext
                        .forTesting(cls, mockMethodRemover, facetHolder));

                val namedFacet = facetHolder.getFacet(ObjectNamedFacet.class);
                assertNull(namedFacet);

                //assertEquals("", namedFacet.translated(NounForm.PLURAL));

                expectNoMethodsRemoved();
            }
        }

    }

}
