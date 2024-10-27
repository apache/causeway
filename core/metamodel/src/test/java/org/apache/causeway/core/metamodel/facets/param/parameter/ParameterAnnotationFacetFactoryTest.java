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
package org.apache.causeway.core.metamodel.facets.param.parameter;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.spec.Specification;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;
import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;

@SuppressWarnings("unused")
class ParameterAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    ParameterAnnotationFacetFactory facetFactory;

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new ParameterAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class MaxLength extends ParameterAnnotationFacetFactoryTest {

        private void test(final Class<?> classUnderTest) {
            // given
            parameterScenario(classUnderTest, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final MaxLengthFacet maxLengthFacet = facetedMethodParameter.getFacet(MaxLengthFacet.class);
                assertNotNull(maxLengthFacet);
                assertTrue(maxLengthFacet instanceof MaxLengthFacetForParameterAnnotation);
                assertThat(maxLengthFacet.value(), is(30));
                // and then
                final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
                assertNotNull(mandatoryFacet);
                assertThat(mandatoryFacet.getSemantics(), is(Semantics.OPTIONAL));
            });
        }

        @Test
        public void withAnnotation() {
            class Customer {
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            test(Customer.class);
        }

        @Test
        public void withInheritedAnnotation() {
            abstract class Base {
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            class Customer extends Base {
            }
            test(Customer.class);
        }

        @Test
        public void withInheritedNonAnnotatedBase() {
            abstract class Base {
                public void someAction(
                        final String name) { }
            }
            class Customer extends Base {
                @Override
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            test(Customer.class);
        }

        @Test
        public void withOverwrittenAnnotation() {
            abstract class Base {
                public void someAction(
                        @Parameter(maxLength = 10)
                        final String name) { }
            }
            class Customer extends Base {
                @Override
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            test(Customer.class);
        }

        @Test
        public void withGenericallyOverwrittenAnnotation() {
            abstract class Base<T> {
                public void someAction(
                        @Parameter(maxLength = 10)
                        final T name) { }
            }
            class Customer extends Base<String> {
                @Override
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            test(Customer.class);
        }

        @Test
        public void withGenericallyOverwrittenNonAnnotatedBase() {
            abstract class Base<T> {
                public void someAction(
                        final T name) { }
            }
            class Customer extends Base<String> {
                @Override
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final String name) { }
            }
            test(Customer.class);
        }

        //[CAUSEWAY-3571] support for generic type resolution on inheriting Customer class
        @Test
        public void withGenericallyInheritedFullyAnnotatedBase() {
            abstract class Base<T> {
                public void someAction(
                        @Parameter(maxLength = 30) @Nullable
                        final T name) { }
            }
            class Customer extends Base<String> {
            }
            test(Customer.class);
        }

    }

    public static class MustSatisfy extends ParameterAnnotationFacetFactoryTest {

        public static class NotTooHot implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }

        public static class NotTooCold implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }

        @Test
        public void withAnnotation() {

            class Customer {
                public void someAction(
                        @Parameter(mustSatisfy = {NotTooHot.class, NotTooCold.class})
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);

                // then
                final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethodParameter.getFacet(MustSatisfySpecificationFacet.class);
                assertNotNull(mustSatisfySpecificationFacet);
                assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForParameterAnnotation);
                MustSatisfySpecificationFacetForParameterAnnotation mustSatisfySpecificationFacetImpl = (MustSatisfySpecificationFacetForParameterAnnotation) mustSatisfySpecificationFacet;
                var specifications = mustSatisfySpecificationFacetImpl.getSpecifications();
                assertThat(specifications.size(), is(2));

                assertTrue(specifications.getElseFail(0) instanceof NotTooHot);
                assertTrue(specifications.getElseFail(1) instanceof NotTooCold);
            });
        }
    }

    public static class Mandatory extends ParameterAnnotationFacetFactoryTest {

        @Test
        public void whenOptionalityIsTrue() {

            class Customer {
                public void someAction(
                        @Parameter(optionality = Optionality.OPTIONAL)
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
                assertNotNull(mandatoryFacet);
                assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Optional);
            });
        }

        @Test
        public void whenOptionalityIsFalse() {

            class Customer {
                public void someAction(
                        @Parameter(optionality = Optionality.MANDATORY)
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
                assertNotNull(mandatoryFacet);
                assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Required);
            });
        }

        @Test
        public void whenOptionalityIsDefault() {

            class Customer {
                public void someAction(
                        @Parameter(optionality = Optionality.DEFAULT)
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
                assertNull(mandatoryFacet);
            });
        }

        @Test
        public void whenNone() {

            class Customer {
                public void someAction(
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
                assertNull(mandatoryFacet);
            });
        }

    }

    public static class RegEx extends ParameterAnnotationFacetFactoryTest {

        @Test
        public void whenHasAnnotation() {

            class Customer {
                public void someAction(
                        @Parameter(
                                regexPattern = "[123].*",
                                regexPatternFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
                assertNotNull(regExFacet);
                assertTrue(regExFacet instanceof RegExFacetForParameterAnnotation);
                assertThat(regExFacet.patternFlags(), is(10));
                assertThat(regExFacet.regexp(), is("[123].*"));

            });
        }

        @Test
        public void whenNone() {

            class Customer {
                public void someAction(
                        @Parameter()
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
                assertNull(regExFacet);

            });
        }

        @Test
        public void whenEmptyString() {

            class Customer {
                public void someAction(
                        @Parameter(regexPattern = "")
                        final String name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
                assertNull(regExFacet);
            });
        }

        @Test
        public void whenNotAnnotatedOnStringParameter() {

            class Customer {
                public void someAction(
                        @Parameter(regexPattern = "[123].*")
                        final int name) {}
            }

            // given
            parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
                // when
                facetFactory.processParams(processParameterContext);
                // then
                final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
                assertNotNull(regExFacet);

            });
        }
    }
}
