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
package org.apache.causeway.core.metamodel.facets.properties.promptstyle;

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

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.PromptStyleFacetForPropertyLayoutAnnotation;

public class PromptStyleFacetFromPropertyAnnotation_Test {

    CausewayConfiguration stubConfiguration = new CausewayConfiguration(null);

    FacetHolder mockFacetHolder;
    PropertyLayout mockPropertyLayout;

    @BeforeEach
    public void setUp() throws Exception {
        mockFacetHolder = Mockito.mock(FacetHolder.class);
        mockPropertyLayout = Mockito.mock(PropertyLayout.class);
    }

    public static class Create_Test extends PromptStyleFacetFromPropertyAnnotation_Test {

        @Test
        public void when_annotated_with_dialog() throws Exception {

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.DIALOG);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(anInstanceOf(PromptStyleFacetForPropertyLayoutAnnotation.class)));
            assertThat(facet.value(), is(PromptStyle.DIALOG));
        }

        @Test
        public void when_annotated_with_inline() throws Exception {

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.INLINE);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(anInstanceOf(PromptStyleFacetForPropertyLayoutAnnotation.class)));
            assertThat(facet.value(), is(PromptStyle.INLINE));
        }

        @Test
        public void when_annotated_with_as_configured() throws Exception {

            stubConfiguration.getViewer().getWicket().setPromptStyle(PromptStyle.INLINE);

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.AS_CONFIGURED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(false);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(anInstanceOf(PromptStyleFacetAsConfigured.class)));
            assertThat(facet.value(), is(PromptStyle.INLINE));
        }

        @Test
        public void when_annotated_with_as_configured_but_already_has_doop_facet() throws Exception {

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.AS_CONFIGURED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(true);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(nullValue()));
        }

        @Test
        public void when_not_annotated() throws Exception {

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.NOT_SPECIFIED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(false);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet.value(), is(PromptStyle.INLINE));
            assertThat(facet, is(anInstanceOf(PromptStyleFacetAsConfigured.class)));
        }

        @Test
        public void when_not_annotated_but_already_has_doop_facet() throws Exception {

            Mockito.when(mockPropertyLayout.promptStyle()).thenReturn(PromptStyle.NOT_SPECIFIED);
            Mockito.when(mockFacetHolder.containsNonFallbackFacet(PromptStyleFacet.class))
            .thenReturn(true);

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Optional.of(mockPropertyLayout), stubConfiguration, mockFacetHolder)
                    .orElse(null);

            assertThat(facet, is(nullValue()));
        }

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


}
