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
package org.apache.isis.core.metamodel.facets.object;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import lombok.val;

public class ViewModelSemanticCheckingFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);


    @Mock @JUnitRuleMockery2.Ignoring
    private ServiceInjector mockServicesInjector;

    private MetaModelContext metaModelContext;
    private ViewModelSemanticCheckingFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {

        val configuration = new IsisConfiguration(null);
        configuration.getApplib().getAnnotation().getViewModel().getValidation().getSemanticChecking().setEnable(true);

        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(configuration)
                .programmingModelFactory(mmc->new ProgrammingModelAbstract(mmc) {})
                .build();

        facetFactory = new ViewModelSemanticCheckingFacetFactory(metaModelContext);
    }

    @Test
    public void whenValidAnnotatedDomainObjectAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotations.DomainObject
        @org.apache.isis.applib.annotations.DomainObjectLayout
        class ValidAnnotatedDomainObjectAndDomainObjectLayout {
        }

        val validationFailures = processThenValidate(ValidAnnotatedDomainObjectAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotations.DomainObject(nature = Nature.VIEW_MODEL)
        class ValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        val validationFailures = processThenValidate(
                ValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenInvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotations.DomainObject(nature = Nature.NOT_SPECIFIED)
        class InvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        val validationFailures = processThenValidate(
                InvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with @DomainObject with nature of NOT_SPECIFIED and also implement RecreatableDomainObject (specify a nature of VIEW_MODEL)"));
    }

    @Test
    public void whenInvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotations.DomainObject(nature = Nature.ENTITY)
        class InvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        val validationFailures = processThenValidate(
                InvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(),
                containsString("should not be annotated with @DomainObject with nature of ENTITY and also implement RecreatableDomainObject (specify a nature of VIEW_MODEL)"));
    }

    // -- HELPER

    private ValidationFailures processThenValidate(final Class<?> cls) {

        val holder = FacetHolderAbstract.forTesting(metaModelContext);
        facetFactory.process(ProcessClassContext.forTesting(cls, null, holder));

        return metaModelContext.getSpecificationLoader().getOrAssessValidationResult();
    }


}