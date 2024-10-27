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
package org.apache.causeway.core.metamodel.facets.object.ident.title;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.Evaluators;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.causeway.core.metamodel.object.ManagedObject;

class TitleAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    private TitleAnnotationFacetFactory facetFactory;

    @BeforeEach
    public void setUp() throws Exception {
        assertNotNull(getInteractionService());
        facetFactory = new TitleAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    // -- SCENARIO 1

    static class Customer1 {
        @Title
        public String someTitle() {
            return "Some Title";
        }
    }

    @Test
    void titleAnnotatedMethodPickedUpOnClassRemoved() throws Exception {

        var someTitleMethod = _GenericResolver.testing
                .resolveMethod(Customer1.class, "someTitle");

        objectScenario(Customer1.class, (processClassContext, facetHolder)->{
            facetFactory.process(processClassContext);

            final Facet facet = facetHolder.getFacet(TitleFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
            final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation =
                    (TitleFacetViaTitleAnnotation) facet;

            final List<ResolvedMethod> titleMethods = Arrays.asList(someTitleMethod);
            for (int i = 0; i < titleMethods.size(); i++) {
                final Evaluators.MethodEvaluator titleEvaluator =
                        (Evaluators.MethodEvaluator) titleFacetViaTitleAnnotation.getComponents()
                        .getElseFail(i)
                        .getTitleEvaluator();

                assertEquals(titleMethods.get(i),
                        titleEvaluator.getMethod());
            }
        });

    }

    // -- SCENARIO 2

    public static class Customer2 { //TODO public should not be a requirement

        @Title(sequence = "1", append = ".")
        public String titleElement1() {
            return "titleElement1";
        }

        @Title(sequence = "2", prepend = ",")
        public String titleElement2() {
            return "titleElement2";
        }

        @Title(sequence = "1.5")
        public String titleElement3() {
            return "titleElement3";
        }

    }

    @Test
    void titleAnnotatedMethodsPickedUpOnClass() throws Exception {

        final List<ResolvedMethod> titleMethods = List.of(
                findMethodExactOrFail(Customer2.class, "titleElement1"),
                findMethodExactOrFail(Customer2.class, "titleElement3"),
                findMethodExactOrFail(Customer2.class, "titleElement2"));

        objectScenario(Customer2.class, (processClassContext, facetHolder)->{
            facetFactory.process(processClassContext);

            final Facet facet = facetHolder.getFacet(TitleFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof TitleFacetViaTitleAnnotation);
            final TitleFacetViaTitleAnnotation titleFacetViaTitleAnnotation =
                    (TitleFacetViaTitleAnnotation) facet;

            for (int i = 0; i < titleMethods.size(); i++) {
                final Evaluators.MethodEvaluator titleEvaluator =
                        (Evaluators.MethodEvaluator) titleFacetViaTitleAnnotation.getComponents()
                        .getElseFail(i)
                        .getTitleEvaluator();

                assertEquals(titleMethods.get(i),
                        titleEvaluator.getMethod());
            }

            final Customer2 customer = new Customer2();
            var objectAdapter = ManagedObject.adaptSingular(getSpecificationLoader(), customer);

            final String title = titleFacetViaTitleAnnotation.title(objectAdapter);
            assertThat(title, is("titleElement1. titleElement3,titleElement2"));

        });

    }

    // -- SCENARIO 3

    static class Customer3 {
    }

    @Test
    void noExplicitTitleAnnotations() {

        objectScenario(Customer3.class, (processClassContext, facetHolder)->{
            facetFactory.process(processClassContext);
            assertNull(facetHolder.getFacet(TitleFacet.class));
        });
    }

    // -- SCENARIO 4

    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Customer4 { //TODO public should not be a requirement

        @Title(sequence = "1")
        public String titleElement1() {
            return "titleElement1";
        }

        @Title(sequence = "2")
        public String titleElement2() {
            return null;
        }

        @Title(sequence = "3")
        public String titleElement3() {
            return "titleElement3";
        }

        @Title(sequence = "4", prepend = "ignored-since-null", append = "ignored-since-null")
        public Object titleElement4() {
            return null;
        }

        @Title(sequence = "4.4", prepend = "ignored-since-empty-string", append = "ignored-since-empty-string")
        public Object titleElement4a() {
            return "";
        }

        @Title(sequence = "5")
        public String titleElement5() {
            return "titleElement5";
        }

        @Title(sequence = "6")
        public Integer titleElement6() {
            return 3;
        }

        @Title(sequence = "7")
        public String titleElement7() {
            return "  this needs to be trimmed      ";
        }

    }

    @Test
    void annotatedMethodsSomeOfWhichReturnNulls() throws Exception {

        { // check prerequisites
            var wThree = ManagedObject.adaptSingular(getSpecificationLoader(), Integer.valueOf(3));
            assertEquals("3", wThree.getTitle());
            var pThree = ManagedObject.adaptSingular(getSpecificationLoader(), 3);
            assertEquals("3", pThree.getTitle());
        }

        objectScenario(Customer4.class, (processClassContext, facetHolder)->{
            facetFactory.process(processClassContext);

            var objectAdapter = getObjectManager().adapt(new Customer4());

            assertThat(objectAdapter.getTitle(),
                    is("titleElement1 titleElement3 titleElement5 3 this needs to be trimmed"));
        });

    }

}
