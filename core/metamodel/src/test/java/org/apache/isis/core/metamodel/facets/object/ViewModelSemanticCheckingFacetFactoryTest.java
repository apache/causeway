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

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ViewModelSemanticCheckingFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);


    @Mock
    private ServicesInjector mockServicesInjector;

    private ViewModelSemanticCheckingFacetFactory facetFactory;

    private ValidationFailures processThenRefine(final Class<?> cls) {
        facetFactory.process(new FacetFactory.ProcessClassContext(cls, null, null));
        final MetaModelValidatorComposite metaModelValidator = new MetaModelValidatorComposite();
        facetFactory.refineMetaModelValidator(metaModelValidator);

        final ValidationFailures validationFailures = new ValidationFailures();
        metaModelValidator.validate(validationFailures);
        return validationFailures;
    }

    @Before
    public void setUp() throws Exception {

        _Config.clear();
        _Config.put("isis.reflector.facets.ViewModelSemanticCheckingFacetFactory.enable", true);
        
        facetFactory = new ViewModelSemanticCheckingFacetFactory();

        facetFactory.setServicesInjector(mockServicesInjector);
    }

    @Test
    public void whenValidAnnotatedWithViewModelAndViewModelLayout() throws Exception {

        @org.apache.isis.applib.annotation.ViewModel
        @org.apache.isis.applib.annotation.ViewModelLayout
        class ValidAnnotatedWithViewModelAndViewModelLayout {
        }

        final ValidationFailures validationFailures = processThenRefine(ValidAnnotatedWithViewModelAndViewModelLayout.class);
        assertThat(validationFailures.getNumberOfMessages(), is(0));
    }

    @Test
    public void whenValidAnnotatedDomainObjectAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject
        @org.apache.isis.applib.annotation.DomainObjectLayout
        class ValidAnnotatedDomainObjectAndDomainObjectLayout {
        }

        final ValidationFailures validationFailures = processThenRefine(ValidAnnotatedDomainObjectAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfMessages(), is(0));
    }

    @Test
    public void whenInvalidAnnotatedViewModelAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotation.ViewModel
        @org.apache.isis.applib.annotation.DomainObjectLayout
        class InvalidAnnotatedViewModelAndDomainObjectLayout {
        }

        final ValidationFailures validationFailures = processThenRefine(InvalidAnnotatedViewModelAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfMessages(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with both @ViewModel and @DomainObjectLayout (annotate with @ViewModelLayout instead of @DomainObjectLayout, or annotate with @DomainObject instead of @ViewModel)"));
    }

    @Test
    public void whenInvalidAnnotatedDomainObjectAndViewModelLayout() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject
        @org.apache.isis.applib.annotation.ViewModelLayout
        class InvalidAnnotatedDomainObjectAndViewModelLayout {
        }

        final ValidationFailures validationFailures = processThenRefine(InvalidAnnotatedDomainObjectAndViewModelLayout.class);
        assertThat(validationFailures.getNumberOfMessages(), is(1));
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

        final ValidationFailures validationFailures = processThenRefine(ValidDomainObjectWithViewModelNatureImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfMessages(), is(0));
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

        final ValidationFailures validationFailures = processThenRefine(ValidDomainObjectWithNatureExternalEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfMessages(), is(0));
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

        final ValidationFailures validationFailures = processThenRefine(ValidDomainObjectWithNatureInmemoryEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfMessages(), is(0));
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

        final ValidationFailures validationFailures = processThenRefine(InvalidDomainObjectWithNatureNotSpecifiedImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfMessages(), is(1));
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

        final ValidationFailures validationFailures = processThenRefine(InvalidDomainObjectWithNatureJdoEntityImplementingRecreatableDomainObject.class);
        assertThat(validationFailures.getNumberOfMessages(), is(1));
        assertThat(validationFailures.getMessages().iterator().next(), containsString("should not be annotated with @DomainObject with nature of JDO_ENTITY and also implement RecreatableDomainObject (specify a nature of EXTERNAL_ENTITY, INMEMORY_ENTITY or VIEW_MODEL)"));
    }



}