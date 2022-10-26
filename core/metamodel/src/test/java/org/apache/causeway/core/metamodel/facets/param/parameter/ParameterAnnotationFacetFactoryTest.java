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

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.calls;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.spec.Specification;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@SuppressWarnings("unused")
class ParameterAnnotationFacetFactoryTest
extends AbstractFacetFactoryJupiterTestCase {

    ParameterAnnotationFacetFactory facetFactory;
    Method actionMethod;

    @Mock ObjectSpecification mockTypeSpec;
    @Mock ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        Mockito.verify(mockMethodRemover, calls(1)).removeMethod(actionMethod);
    }

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new ParameterAnnotationFacetFactory(metaModelContext);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class MaxLength extends ParameterAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                public void someAction(
                        @Parameter(
                                maxLength = 30
                                )
                        final String name) { }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MaxLengthFacet maxLengthFacet = facetedMethodParameter.getFacet(MaxLengthFacet.class);
            assertNotNull(maxLengthFacet);
            assertTrue(maxLengthFacet instanceof MaxLengthFacetForParameterAnnotation);
            assertThat(maxLengthFacet.value(), is(30));
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
                        @Parameter(
                                mustSatisfy = {NotTooHot.class, NotTooCold.class}
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethodParameter.getFacet(MustSatisfySpecificationFacet.class);
            assertNotNull(mustSatisfySpecificationFacet);
            assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForParameterAnnotation);
            MustSatisfySpecificationFacetForParameterAnnotation mustSatisfySpecificationFacetImpl = (MustSatisfySpecificationFacetForParameterAnnotation) mustSatisfySpecificationFacet;
            val specifications = mustSatisfySpecificationFacetImpl.getSpecifications();
            assertThat(specifications.size(), is(2));

            assertTrue(specifications.getElseFail(0) instanceof NotTooHot);
            assertTrue(specifications.getElseFail(1) instanceof NotTooCold);
        }

    }

    public static class Mandatory extends ParameterAnnotationFacetFactoryTest {

        @Test
        public void whenOptionalityIsTrue() {

            class Customer {
                public void someAction(
                        @Parameter(
                                optionality = Optionality.OPTIONAL
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            assertNotNull(mandatoryFacet);
            assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Optional);
        }

        @Test
        public void whenOptionalityIsFalse() {

            class Customer {
                public void someAction(
                        @Parameter(
                                optionality = Optionality.MANDATORY
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            assertNotNull(mandatoryFacet);
            assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Required);
        }

        @Test
        public void whenOptionalityIsDefault() {

            class Customer {
                public void someAction(
                        @Parameter(
                                optionality = Optionality.DEFAULT
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            assertNull(mandatoryFacet);
        }

        @Test
        public void whenNone() {

            class Customer {
                public void someAction(
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            assertNull(mandatoryFacet);
        }

    }

    public static class RegEx extends ParameterAnnotationFacetFactoryTest {

        @Test
        public void whenHasAnnotation() {

            class Customer {
                public void someAction(
                        @Parameter(
                                regexPattern = "[123].*",
                                regexPatternFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            assertNotNull(regExFacet);
            assertTrue(regExFacet instanceof RegExFacetForParameterAnnotation);
            assertThat(regExFacet.patternFlags(), is(10));
            assertThat(regExFacet.regexp(), is("[123].*"));
        }

        @Test
        public void whenNone() {

            class Customer {
                public void someAction(
                        @Parameter(
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            assertNull(regExFacet);
        }

        @Test
        public void whenEmptyString() {

            class Customer {
                public void someAction(
                        @Parameter(
                                regexPattern = ""
                                )
                        final String name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            assertNull(regExFacet);
        }

        @Test
        public void whenNotAnnotatedOnStringParameter() {

            class Customer {
                public void someAction(
                        @Parameter(
                                regexPattern = "[123].*"
                                )
                        final int name
                        ) {
                }
            }

            // given
            actionMethod = findMethod(Customer.class, "someAction", new Class[]{int.class} );

            // when
            final FacetFactory.ProcessParameterContext processParameterContext =
                    new FacetFactory.ProcessParameterContext(
                            Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            assertNotNull(regExFacet);

        }

    }

}