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
package org.apache.causeway.persistence.jdo.metamodel.facets.object.version;

import javax.jdo.annotations.Version;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel.FacetProcessingOrder;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.causeway.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;

import lombok.val;

class JdoVersionAnnotationFacetFactoryTest_validate {

    private MetaModelContext_forTesting metaModelContext;

    @BeforeEach
    void setUp() throws Exception {

        val configuration = new CausewayConfiguration(null);

        metaModelContext = MetaModelContext_forTesting
        .builder()
        .programmingModelFactory(mmc->{

            val programmingModel = new ProgrammingModelAbstract(mmc) {};

            val facetFactory = new JdoVersionAnnotationFacetFactory(
                    metaModelContext,
                    AbstractFacetFactoryTest.jdoFacetContextForTesting());


            programmingModel
            .addFactory(FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS, facetFactory);

            return programmingModel;

        })
        .configuration(configuration)
        .build();

    }

    @Test
    void whenNoFacet() {

        class Child {}

        val failures = processThenValidate(Child.class);
        assertThat(failures.getNumberOfFailures(), is(0));
    }

    @Test
    void whenHasFacetNoSuperType() {

        @Version
        class Child {}

        val failures = processThenValidate(Child.class);
        assertThat(failures.getNumberOfFailures(), is(0));
    }

    @Test
    void whenHasFacetWithSuperTypeHasNoFacet() {

        class Parent {}

        @Version
        class Child extends Parent {}

        val failures = processThenValidate(Child.class);
        assertThat(failures.getNumberOfFailures(), is(0));
    }


    @Test
    void whenHasFacetWithParentTypeHasFacet() {

        @Version
        class Parent {}

        @Version
        class Child extends Parent {}

        val failures = processThenValidate(Child.class);

        assertThat(failures.getNumberOfFailures(), is(1));
        assertThat(failures.getMessages().iterator().next(),
                CoreMatchers.containsString("@Version annotation is ambiguous within a class hierarchy"));
    }


    @Test
    void whenHasFacetWithGrandParentTypeHasFacet() {

        @Version
        class GrandParent {}

        class Parent extends GrandParent {}

        @Version
        class Child extends Parent {}

        val failures = processThenValidate(Child.class);

        assertTrue(failures.getNumberOfFailures()>=1);
        assertThat(failures.getMessages().iterator().next(),
                CoreMatchers.containsString("@Version annotation is ambiguous within a class hierarchy"));
    }

    @Test
    void whenHasFacetWithAbstactParentTypeHasFacet() {

        @Version
        abstract class Parent {}

        class Child extends Parent {}

        val failures = processThenValidate(Child.class);

        assertThat(failures.getNumberOfFailures(), is(0));
    }

    private ValidationFailures processThenValidate(final Class<?> cls) {
        val specLoader = metaModelContext.getSpecificationLoader();
        specLoader.specForType(cls).get(); // fail if empty
        return specLoader.getOrAssessValidationResult();
    }

}
