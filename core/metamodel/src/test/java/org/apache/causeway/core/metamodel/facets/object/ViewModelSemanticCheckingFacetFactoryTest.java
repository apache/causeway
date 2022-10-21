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
package org.apache.causeway.core.metamodel.facets.object;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;

import lombok.val;

class ViewModelSemanticCheckingFacetFactoryTest {

    @Mock
    private ServiceInjector mockServicesInjector;

    private MetaModelContext metaModelContext;
    private ViewModelSemanticCheckingFacetFactory facetFactory;

    @BeforeEach
    public void setUp() throws Exception {

        val configuration = new CausewayConfiguration(null);
        configuration.getApplib().getAnnotation().getViewModel().getValidation().getSemanticChecking().setEnable(true);

        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(configuration)
                .programmingModelFactory(mmc->new ProgrammingModelAbstract(mmc) {})
                .build();

        facetFactory = new ViewModelSemanticCheckingFacetFactory(metaModelContext);
    }

    @Test
    public void whenValidAnnotatedDomainObjectAndDomainObjectLayout() throws Exception {

        @org.apache.causeway.applib.annotation.DomainObject
        @org.apache.causeway.applib.annotation.DomainObjectLayout
        class ValidAnnotatedDomainObjectAndDomainObjectLayout {
        }

        val validationFailures = processThenValidate(ValidAnnotatedDomainObjectAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    // -- HELPER

    private ValidationFailures processThenValidate(final Class<?> cls) {

        val holder = FacetHolder.forTesting(metaModelContext);
        facetFactory.process(ProcessClassContext.forTesting(cls, null, holder));

        return metaModelContext.getSpecificationLoader().getOrAssessValidationResult();
    }


}