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
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.unittestsupport.config.internal._Config;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import lombok.val;

public class ViewModelSemanticCheckingFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);


    @Mock @JUnitRuleMockery2.Ignoring
    private ServiceInjector mockServicesInjector;

    private MetaModelContext metaModelContext;
    private ViewModelSemanticCheckingFacetFactory facetFactory;

    private ValidationFailures processThenValidate(final Class<?> cls) {
        
        val programmingModel = new ProgrammingModelAbstract(mockServicesInjector) {};
        facetFactory.refineProgrammingModel(programmingModel);
        programmingModel.init(new ProgrammingModelInitFilterDefault(), metaModelContext);
        
        val holder = new FacetHolderImpl();
        ((MetaModelContextAware)holder).setMetaModelContext(metaModelContext);
        facetFactory.process(new FacetFactory.ProcessClassContext(cls, null, holder));
        
        val validationFailures = new ValidationFailures();
        
        programmingModel.streamValidators()
        .forEach(validator->((MetaModelValidatorAbstract)validator).collectFailuresInto(validationFailures));

        return validationFailures;
    }

    @Before
    public void setUp() throws Exception {

        _Context.clear();
        _Config.clear();

        val configuration = new IsisConfiguration();
        configuration.getApplib().getAnnotation().getViewModel().getValidation().getViewModelSemanticChecking().setEnable(true);

        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(configuration)
                .build();

        facetFactory = new ViewModelSemanticCheckingFacetFactory();
        facetFactory.setMetaModelContext(metaModelContext);
    }

    @Test
    public void whenValidAnnotatedWithViewModelAndViewModelLayout() throws Exception {

        @org.apache.isis.applib.annotation.ViewModel
        @org.apache.isis.applib.annotation.ViewModelLayout
        class ValidAnnotatedWithViewModelAndViewModelLayout {
        }

        final ValidationFailures validationFailures = processThenValidate(ValidAnnotatedWithViewModelAndViewModelLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenValidAnnotatedDomainObjectAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject
        @org.apache.isis.applib.annotation.DomainObjectLayout
        class ValidAnnotatedDomainObjectAndDomainObjectLayout {
        }

        final ValidationFailures validationFailures = processThenValidate(ValidAnnotatedDomainObjectAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenInvalidAnnotatedViewModelAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotation.ViewModel
        @org.apache.isis.applib.annotation.DomainObjectLayout
        class InvalidAnnotatedViewModelAndDomainObjectLayout {
        }

        final ValidationFailures validationFailures = processThenValidate(InvalidAnnotatedViewModelAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with both @ViewModel and @DomainObjectLayout (annotate with @ViewModelLayout instead of @DomainObjectLayout, or annotate with @DomainObject instead of @ViewModel)"));
    }

    @Test
    public void whenInvalidAnnotatedDomainObjectAndViewModelLayout() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject
        @org.apache.isis.applib.annotation.ViewModelLayout
        class InvalidAnnotatedDomainObjectAndViewModelLayout {
        }

        final ValidationFailures validationFailures = processThenValidate(InvalidAnnotatedDomainObjectAndViewModelLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with @ViewModelLayout and also be annotated with @DomainObject (annotate with @ViewModel instead of @DomainObject, or instead annotate with @DomainObjectLayout instead of @ViewModelLayout)"));
    }

    @Test
    public void whenValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject(nature = Nature.VIEW_MODEL)
        class ValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        final ValidationFailures validationFailures = processThenValidate(ValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenValidDomainObjectWithNatureExternalEntityImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject(nature = Nature.EXTERNAL_ENTITY)
        class ValidDomainObjectWithNatureExternalEntityImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        final ValidationFailures validationFailures = processThenValidate(ValidDomainObjectWithNatureExternalEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenValidDomainObjectWithNatureInmemoryEntityImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject(nature = Nature.INMEMORY_ENTITY)
        class ValidDomainObjectWithNatureInmemoryEntityImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        final ValidationFailures validationFailures = processThenValidate(ValidDomainObjectWithNatureInmemoryEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenInvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject(nature = Nature.NOT_SPECIFIED)
        class InvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        final ValidationFailures validationFailures = processThenValidate(InvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with @DomainObject with nature of NOT_SPECIFIED and also implement RecreatableDomainObject (specify a nature of EXTERNAL_ENTITY, INMEMORY_ENTITY or VIEW_MODEL)"));
    }

    @Test
    public void whenInvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject(nature = Nature.JDO_ENTITY)
        class InvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject implements RecreatableDomainObject {
            @Override
            public String __isis_memento() {
                return null;
            }
            @Override
            public void __isis_recreate(final String memento) {
            }
        }

        final ValidationFailures validationFailures = processThenValidate(InvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfFailures(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with @DomainObject with nature of JDO_ENTITY and also implement RecreatableDomainObject (specify a nature of EXTERNAL_ENTITY, INMEMORY_ENTITY or VIEW_MODEL)"));
    }



}