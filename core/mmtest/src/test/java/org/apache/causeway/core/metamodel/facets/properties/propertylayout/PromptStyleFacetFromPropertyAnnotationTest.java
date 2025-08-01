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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.springframework.boot.test.util.TestPropertyValues;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.mmtestsupport.ConfigurationTester;

class PromptStyleFacetFromPropertyAnnotationTest {

    FacetHolder mockFacetHolder;
    PropertyLayout mockPropertyLayout;

    @BeforeEach
    void setUp() throws Exception {
        mockFacetHolder = Mockito.mock(FacetHolder.class);
        mockPropertyLayout = Mockito.mock(PropertyLayout.class);
    }

    @Test
    void when_annotated_with_dialog() throws Exception {
        new ConfigurationTester(TestPropertyValues.empty())
        .test(conf->{
            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.DIALOG);

            PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                        Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                    .orElse(null);

            assertThat(facet.origin(), is("PropertyLayoutAnnotation"));
            assertThat(facet.value(), is(PromptStyle.DIALOG));
        });
    }

    @Test
    void when_annotated_with_inline() throws Exception {
        new ConfigurationTester(TestPropertyValues.empty())
            .test(conf->{
                Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.INLINE);

                PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                            Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                        .orElse(null);

                assertThat(facet.origin(), is("PropertyLayoutAnnotation"));
                assertThat(facet.value(), is(PromptStyle.INLINE));
            });
    }

    @Test
    void when_annotated_with_as_configured() throws Exception {
        new ConfigurationTester(TestPropertyValues.of("causeway.viewer.wicket.promptStyle=INLINE"))
            .test(conf->{
                Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.AS_CONFIGURED);
                Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
                .thenReturn(false);

                PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                            Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                        .orElse(null);

                assertThat(facet, is(anInstanceOf(PromptStyleFacet.class)));
                assertThat(facet.origin(), is("Configuration"));
                assertThat(facet.value(), is(PromptStyle.INLINE));
            });
    }

    @Test
    void when_annotated_with_as_configured_but_already_has_doop_facet() throws Exception {
        new ConfigurationTester(TestPropertyValues.empty())
        .test(conf->{
            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.AS_CONFIGURED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(true);

            PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                        Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(nullValue()));
        });
    }

    @Test
    void when_not_annotated() throws Exception {
        new ConfigurationTester(TestPropertyValues.empty())
        .test(conf->{
            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.NOT_SPECIFIED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(false);

            PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                        Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                    .orElse(null);


            assertThat(facet, is(anInstanceOf(PromptStyleFacet.class)));
            assertThat(facet.origin(), is("Configuration"));
            assertThat(facet.value(), is(PromptStyle.INLINE));
        });
    }

    @Test
    void when_not_annotated_but_already_has_doop_facet() throws Exception {
        new ConfigurationTester(TestPropertyValues.empty())
        .test(conf->{
            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.NOT_SPECIFIED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(true);

            PromptStyleFacet facet = createPromptStyleFacetForPropertyLayoutAnnotation(
                        Optional.of(mockPropertyLayout), conf, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(nullValue()));
        });
    }

    static <T> Matcher<? super T> anInstanceOf(final Class<T> expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            public boolean matchesSafely(final T actual) {
                return expected.isAssignableFrom(actual.getClass());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("an instance of ").appendValue(expected);
            }
        };
    }

    static Optional<PromptStyleFacet> createPromptStyleFacetForPropertyLayoutAnnotation(
        final Optional<PropertyLayout> p, final CausewayConfiguration c, final FacetHolder f) {
        return PropertyLayoutFacetFactory.createPromptStyleFacetForPropertyLayoutAnnotation(p, c, f);
    }

}
