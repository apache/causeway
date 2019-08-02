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
package org.apache.isis.metamodel.facets.properties.promptstyle;

import java.util.Collections;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleFacetAsConfigured;
import org.apache.isis.metamodel.facets.properties.propertylayout.PromptStyleFacetForPropertyLayoutAnnotation;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class PromptStyleFacetFromPropertyAnnotation_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    IsisConfiguration mockConfiguration;

    @Mock
    FacetHolder mockFacetHolder;

    @Mock
    PropertyLayout mockPropertyLayout;

    public static class Create_Test extends PromptStyleFacetFromPropertyAnnotation_Test {

        @Test
        public void when_annotated_with_dialog() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.DIALOG));

                never(mockConfiguration);
            }});

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet, is((Matcher) anInstanceOf(PromptStyleFacetForPropertyLayoutAnnotation.class)));
            Assert.assertThat(facet.value(), is(PromptStyle.DIALOG));
        }

        @Test
        public void when_annotated_with_inline() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.INLINE));

                never(mockConfiguration);
            }});


            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet, is((Matcher) anInstanceOf(PromptStyleFacetForPropertyLayoutAnnotation.class)));
            Assert.assertThat(facet.value(), is(PromptStyle.INLINE));
        }

        @Test
        public void when_annotated_with_as_configured() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.AS_CONFIGURED));

                oneOf(mockConfiguration).getString("isis.viewer.wicket.promptStyle");
                will(returnValue(PromptStyle.INLINE.name()));

                allowing(mockFacetHolder).containsDoOpFacet(PromptStyleFacet.class);
                will(returnValue(false));
            }});

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet, is((Matcher) anInstanceOf(PromptStyleFacetAsConfigured.class)));
            Assert.assertThat(facet.value(), is(PromptStyle.INLINE));
        }

        @Test
        public void when_annotated_with_as_configured_but_already_has_doop_facet() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.AS_CONFIGURED));

                allowing(mockFacetHolder).containsDoOpFacet(PromptStyleFacet.class);
                will(returnValue(true));

                allowing(mockConfiguration).getString("isis.viewer.wicket.promptStyle");
                will(returnValue(null));

            }});

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet, is(nullValue()));
        }

        @Test
        public void when_not_annotated() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.NOT_SPECIFIED));

                allowing(mockFacetHolder).containsDoOpFacet(PromptStyleFacet.class);
                will(returnValue(false));

                oneOf(mockConfiguration).getString("isis.viewer.wicket.promptStyle");
                will(returnValue(null));
            }});

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet.value(), is(PromptStyle.INLINE));
            Assert.assertThat(facet, is((Matcher) anInstanceOf(PromptStyleFacetAsConfigured.class)));
        }

        @Test
        public void when_not_annotated_but_already_has_doop_facet() throws Exception {

            context.checking(new Expectations() {{
                allowing(mockPropertyLayout).promptStyle();
                will(returnValue(PromptStyle.NOT_SPECIFIED));

                allowing(mockFacetHolder).containsDoOpFacet(PromptStyleFacet.class);
                will(returnValue(true));

                allowing(mockConfiguration).getString("isis.viewer.wicket.promptStyle");
                will(returnValue(null));

            }});

            PromptStyleFacet facet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(Collections.singletonList(mockPropertyLayout), mockConfiguration, mockFacetHolder);

            Assert.assertThat(facet, is(nullValue()));
        }


    }

    static <T> Matcher<T> anInstanceOf(final Class<T> expected) {
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
