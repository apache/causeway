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
package org.apache.isis.core.metamodel.facets.param.parameter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.config.IsisConfiguration.Core.MetaModel.EncapsulationPolicy;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@SuppressWarnings("unused")
public class ParameterAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    ParameterAnnotationFacetFactory facetFactory;
    Method actionMethod;

    @Mock ObjectSpecification mockTypeSpec;
    @Mock ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoader).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));
        }});
    }

    @Before
    public void setUp() throws Exception {
        facetFactory = new ParameterAnnotationFacetFactory(metaModelContext);
    }

    @Override
    @After
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MaxLengthFacet maxLengthFacet = facetedMethodParameter.getFacet(MaxLengthFacet.class);
            Assert.assertNotNull(maxLengthFacet);
            Assert.assertTrue(maxLengthFacet instanceof MaxLengthFacetForParameterAnnotation);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethodParameter.getFacet(MustSatisfySpecificationFacet.class);
            Assert.assertNotNull(mustSatisfySpecificationFacet);
            Assert.assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForParameterAnnotation);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Optional);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForParameterAnnotation.Required);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final MandatoryFacet mandatoryFacet = facetedMethodParameter.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class,EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            Assert.assertNotNull(regExFacet);
            Assert.assertTrue(regExFacet instanceof RegExFacetForParameterAnnotation);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);

            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
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
            final FacetFactory.ProcessParameterContext processParameterContext = new FacetFactory.ProcessParameterContext(Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
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
                            Customer.class, EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, actionMethod, 0, null, facetedMethodParameter);
            facetFactory.processParams(processParameterContext);


            // then
            final RegExFacet regExFacet = facetedMethodParameter.getFacet(RegExFacet.class);
            Assert.assertNotNull(regExFacet);

        }

    }

}