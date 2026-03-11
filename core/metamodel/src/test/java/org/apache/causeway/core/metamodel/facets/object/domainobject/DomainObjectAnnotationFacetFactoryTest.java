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
package org.apache.causeway.core.metamodel.facets.object.domainobject;

import java.util.UUID;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Publishing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.AbstractTestWithMetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotationAsConfigured;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.causeway.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotationAsConfigured;
import org.apache.causeway.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetFromConfiguration;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;
import org.apache.causeway.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;

class DomainObjectAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    DomainObjectAnnotationFacetFactory facetFactory;

    @BeforeEach
    void setUp() throws Exception {
        facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        facetFactory = null;
    }

    static class Customer {
    }

    class SomeHasInteractionId implements HasInteractionId {

        @Override
        public UUID getInteractionId() {
            return null;
        }

    }

    void allowingEntityChangePublishingToReturn(final DomainObjectConfigOptions.EntityChangePublishingPolicy value) {
        if(value!=null) {
            getConfiguration()
                .getApplib().getAnnotation().getDomainObject().setEntityChangePublishing(value);
        }
    }

    void allowingObjectsEditingToReturn(final DomainObjectConfigOptions.EditingObjectsConfiguration value) {
        if(value!=null) {
            getConfiguration()
                .getApplib().getAnnotation().getDomainObject().setEditing(value);
        }
    }

    protected void ignoringConfiguration() {

    }

    @Nested
    public class EntityChangePublishing  {

        @DomainObject(entityChangePublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED)
        class CustomerWithDomainObjectAndAuditingSetToAsConfigured {
        }

        @DomainObject(entityChangePublishing = org.apache.causeway.applib.annotation.Publishing.DISABLED)
        class CustomerWithDomainObjectAndAuditingSetToDisabled {
        }

        @DomainObject(entityChangePublishing = org.apache.causeway.applib.annotation.Publishing.ENABLED)
        class CustomerWithDomainObjectAndAuditingSetToEnabled {
        }

        @DomainObject(entityChangePublishing = Publishing.ENABLED_FOR_UPDATES_ONLY)
        class CustomerWithDomainObjectAndEntityChangePublishingSetToEnabledForUpdatesOnly {
        }


        @Nested
        public class WhenNotAnnotatedAndDefaultsFromConfiguration {

            @Test
            void configured_value_set_to_all() {
                allowingEntityChangePublishingToReturn(DomainObjectConfigOptions.EntityChangePublishingPolicy.ALL);
                objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                    facetFactory.processEntityChangePublishing(
                            processClassContext.synthesizeOnType(DomainObject.class), processClassContext);

                    final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertThat(facet, is(notNullValue()));
                    assertTrue(facet instanceof EntityChangePublishingFacetFromConfiguration);
                    assertThat(facet.isEnabled(), is(true));

                    assertNoMethodsRemoved();
                });
            }

            @Test
            void configured_value_set_to_none() {
                allowingEntityChangePublishingToReturn(DomainObjectConfigOptions.EntityChangePublishingPolicy.NONE);
                objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertNotNull(facet);
                    assertThat(facet.isEnabled(), is(false));

                    assertNoMethodsRemoved();
                });
            }

        }

        @Nested
        public class WithDomainObjectAnnotationWithAuditingSetToAsConfigured {

            @Test
            public void configured_value_set_to_all() {
                allowingEntityChangePublishingToReturn(DomainObjectConfigOptions.EntityChangePublishingPolicy.ALL);
                objectScenario(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof EntityChangePublishingFacetForDomainObjectAnnotationAsConfigured);
                    assertThat(facet.isEnabled(), is(true));

                    assertNoMethodsRemoved();
                });
            }

            @Test
            public void configured_value_set_to_none() {
                allowingEntityChangePublishingToReturn(DomainObjectConfigOptions.EntityChangePublishingPolicy.NONE);
                objectScenario(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertNotNull(facet);
                    assertThat(facet.isEnabled(), is(false));

                    assertNoMethodsRemoved();
                });
            }

        }

        @Nested
        public class WithDomainObjectAnnotationWithAuditingSetToEnabled {

            @Test
            public void irrespective_of_configured_value() {
                allowingEntityChangePublishingToReturn(null);
                objectScenario(CustomerWithDomainObjectAndAuditingSetToEnabled.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof EntityChangePublishingFacetForDomainObjectAnnotation);

                    assertNoMethodsRemoved();
                });
            }

        }

        @Nested
        public class WithDomainObjectAnnotationWithEntityChangePublishingSetToEnabledForUpdatesOnly {

            @Test
            public void irrespective_of_configured_value() {
                allowingEntityChangePublishingToReturn(null);
                objectScenario(CustomerWithDomainObjectAndEntityChangePublishingSetToEnabledForUpdatesOnly.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof EntityChangePublishingFacetForDomainObjectAnnotation);

                    assertFalse(EntityChangePublishingFacet.isPublishingEnabledForCreate(facetHolder));
                    assertTrue(EntityChangePublishingFacet.isPublishingEnabledForUpdate(facetHolder));
                    assertFalse(EntityChangePublishingFacet.isPublishingEnabledForDelete(facetHolder));

                    assertNoMethodsRemoved();
                });
            }

        }

        @Nested
        public class WithDomainObjectAnnotationWithAuditingSetToDisabled {

            @Test
            public void irrespective_of_configured_value() {
                allowingEntityChangePublishingToReturn(DomainObjectConfigOptions.EntityChangePublishingPolicy.ALL);
                objectScenario(CustomerWithDomainObjectAndAuditingSetToDisabled.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    assertFalse(EntityChangePublishingFacet.isPublishingEnabled(facetHolder));

                    assertNoMethodsRemoved();
                });
            }

        }

    }

    @Nested
    public class AutoComplete  {

        class CustomerRepository {
            public String lookup(final String x) { return null; }
        }

        class CustomerRepositoryWithDefaultMethodName {
            public String autoComplete(final String x) { return null; }
        }

        @DomainObject(autoCompleteRepository = CustomerRepository.class, autoCompleteMethod = "lookup")
        class CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction {
        }

        @DomainObject(autoCompleteRepository = CustomerRepositoryWithDefaultMethodName.class)
        class CustomerWithDomainObjectAndAutoCompleteRepository {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoAutoCompleteRepository {
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepositoryAndAction() {

            objectScenario(CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
                assertNotNull(facet);

                assertTrue(facet instanceof AutoCompleteFacetForDomainObjectAnnotation);

                final AutoCompleteFacetForDomainObjectAnnotation autoCompleteFacet = (AutoCompleteFacetForDomainObjectAnnotation) facet;

                assertThat(CustomerRepository.class.isAssignableFrom(autoCompleteFacet.getRepositoryClass()), is(true));
                assertThat(autoCompleteFacet.getActionName(), is("lookup"));

                assertNoMethodsRemoved();
            });

        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepository() {

            objectScenario(CustomerWithDomainObjectAndAutoCompleteRepository.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
                assertNotNull(facet);

                assertTrue(facet instanceof AutoCompleteFacetForDomainObjectAnnotation);

                final AutoCompleteFacetForDomainObjectAnnotation autoCompleteFacet = (AutoCompleteFacetForDomainObjectAnnotation) facet;

                assertThat(CustomerRepositoryWithDefaultMethodName.class.isAssignableFrom(autoCompleteFacet.getRepositoryClass()), is(true));
                assertThat(autoCompleteFacet.getActionName(), is("autoComplete"));

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenDomainObjectAnnotationButNoAutoComplete() {

            objectScenario(CustomerWithDomainObjectButNoAutoCompleteRepository.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

    }

    @Nested
    public class Bounded  {

        @DomainObject(bounding = Bounding.BOUNDED)
        class CustomerWithDomainObjectAndBoundedSetToTrue {
        }

        @DomainObject(bounding = Bounding.UNBOUNDED)
        class CustomerWithDomainObjectAndBoundedSetToFalse {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoBounded {
        }

        @BeforeEach
        public void setUp() throws Exception {
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndBoundedSetToTrue() {

            objectScenario(CustomerWithDomainObjectAndBoundedSetToTrue.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
                assertNotNull(facet);

                assertTrue(facet instanceof ChoicesFacetForDomainObjectAnnotation);

                assertNoMethodsRemoved();

            });
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepository() {

            objectScenario(CustomerWithDomainObjectAndBoundedSetToFalse.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

    }

    @DomainObject(editing = org.apache.causeway.applib.annotation.Editing.AS_CONFIGURED)
    static class CustomerWithDomainObjectAndEditingSetToAsConfigured {
    }

    @DomainObject(editing = org.apache.causeway.applib.annotation.Editing.DISABLED)
    static class CustomerWithDomainObjectAndEditingSetToDisabled {
    }

    @DomainObject(editing = org.apache.causeway.applib.annotation.Editing.ENABLED)
    static class CustomerWithDomainObjectAndEditingSetToEnabled {
    }

    @Nested
    public class Editing {

        @Nested
        public class WhenNotAnnotatedAndDefaultsFromConfiguration {

            @Test
            public void configured_value_set_to_true() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.TRUE);

                objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNull(facet);

                    assertNoMethodsRemoved();

                });
            }

            @Test
            public void configured_value_set_to_false() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.FALSE);

                objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof ImmutableFacetFromConfiguration);

                    assertNoMethodsRemoved();
                });
            }

            @Test
            public void configured_value_set_to_defaults() {
                //allowingConfigurationToReturn("causeway.objects.editing", "foobar");
                objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNotNull(facet); // default is now non-editable
                    assertTrue(facet instanceof ImmutableFacetFromConfiguration);


                    assertNoMethodsRemoved();
                });
            }
        }

        @Nested
        public class WithDomainObjectAnnotationWithEditingSetToAsConfigured  {

            @Test
            public void configured_value_set_to_true() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.TRUE);
                objectScenario(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNull(facet);

                    assertNoMethodsRemoved();
                });
            }

            @Test
            public void configured_value_set_to_false() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.FALSE);
                objectScenario(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotation);

                    assertNoMethodsRemoved();
                });
            }

            @Test
            public void configured_value_set_to_defaults() {
                //allowingConfigurationToReturn("causeway.objects.editing", "foobar");
                objectScenario(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNotNull(facet); // default is now non-editable
                    assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotationAsConfigured);

                    assertNoMethodsRemoved();
                });
            }
        }

        @Nested
        public class WithDomainObjectAnnotationWithEditingSetToEnabled  {

            @Test
            public void irrespective_of_configured_value() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.FALSE);
                objectScenario(CustomerWithDomainObjectAndEditingSetToEnabled.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final ImmutableFacet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNull(facet);

                    assertNoMethodsRemoved();
                });
            }
        }

        @Nested
        public class WithDomainObjectAnnotationWithEditingSetToDisabled  {

            @Test
            public void irrespective_of_configured_value() {
                allowingObjectsEditingToReturn(DomainObjectConfigOptions.EditingObjectsConfiguration.TRUE);
                objectScenario(CustomerWithDomainObjectAndEditingSetToDisabled.class, (processClassContext, facetHolder)->{
                    facetFactory.process(processClassContext);

                    final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                    assertNotNull(facet);
                    assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotation);

                    assertNoMethodsRemoved();
                });
            }
        }
    }

    @Named("CUS")
    @DomainObject
    static class LogicalTypeNameCustomerWithDomainObjectAndObjectTypeSet {
    }

    @DomainObject
    static class LogicalTypeNameCustomerWithDomainObjectButNoObjectType {
    }


    @Nested
    public class LogicalTypeName {

        @BeforeEach
        public void setUp() throws Exception {
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndObjectTypeSetToTrue() {
            assertThat(LogicalType.infer(LogicalTypeNameCustomerWithDomainObjectAndObjectTypeSet.class).logicalName(),
                    is("CUS"));
            assertNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndObjectTypeNotSet() {

            objectScenario(LogicalTypeNameCustomerWithDomainObjectButNoObjectType.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AliasedFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(AliasedFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

    }


    @DomainObject(nature = org.apache.causeway.applib.annotation.Nature.ENTITY)
    static class CustomerWithDomainObjectAndNatureSetToJdoEntity {
    }

    @DomainObject(nature = org.apache.causeway.applib.annotation.Nature.NOT_SPECIFIED)
    static class CustomerWithDomainObjectAndNatureSetToNotSpecified {
    }

    @DomainObject(nature = org.apache.causeway.applib.annotation.Nature.VIEW_MODEL)
    static class CustomerWithDomainObjectAndNatureSetToViewModel {
    }

    @DomainObject
    static class CustomerWithDomainObjectButNoNature {
    }

    @Nested
    public class Nature  {

        @BeforeEach
        public void setUp() throws Exception {
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndNatureSetToJdoEntity() {

            objectScenario(CustomerWithDomainObjectAndNatureSetToJdoEntity.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenDomainObjectAndNatureSetToNotSpecified() {

            objectScenario(CustomerWithDomainObjectAndNatureSetToNotSpecified.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });
        }

        @Test
        public void whenDomainObjectAndNatureSetToViewModel() {

            objectScenario(CustomerWithDomainObjectAndNatureSetToViewModel.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
                assertNotNull(facet);

                assertTrue(facet instanceof ViewModelFacetForDomainObjectAnnotation);

                assertNoMethodsRemoved();
            });

        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            objectScenario(DomainObjectAnnotationFacetFactoryTest.Customer.class, (processClassContext, facetHolder)->{
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
                assertNull(facet);

                assertNoMethodsRemoved();
            });

        }

    }

    @Named("object.name")
    @DomainObject(aliased = {"object.name", "object.alias"})
    static class AliasDomainObjectWithAliases {
    }

    @Named("service.name")
    @DomainService(aliased = {"service.name", "service.alias"})
    static class AliasDomainServiceWithAliases {
    }


    @Nested
    public class Alias  {
        DomainObjectAnnotationFacetFactory facetFactory;

        @Test
        public void testValidationDomainObjectWithAliasesConfigured() {
            metaModelContext = MetaModelContext_forTesting.builder()
                    .refiners(Can.of(__->facetFactory))
                    .build();
            getConfiguration().getCore().getMetaModel().getValidator().setAllowLogicalTypeNameAsAlias(true);
            facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
            ((MetaModelContext_forTesting) getMetaModelContext()).getProgrammingModel();//kicks off the programming model factory

            getMetaModelContext().getSpecificationLoader().loadSpecification(AliasDomainObjectWithAliases.class, IntrospectionState.FULLY_INTROSPECTED);
            ValidationFailures validationFailures = getMetaModelContext().getSpecificationLoader().getOrAssessValidationResult();
            assertFalse(validationFailures.hasFailures());
        }

        @Test
        public void testValidationDomainServiceWithAliasesConfigured() {
            metaModelContext = MetaModelContext_forTesting.builder()
                    .refiners(Can.of(__->facetFactory))
                    .build();
            getConfiguration().getCore().getMetaModel().getValidator().setAllowLogicalTypeNameAsAlias(true);
            facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
            ((MetaModelContext_forTesting) getMetaModelContext()).getProgrammingModel();//kicks off the programming model factory

            getMetaModelContext().getSpecificationLoader().loadSpecification(AliasDomainServiceWithAliases.class, IntrospectionState.FULLY_INTROSPECTED);
            ValidationFailures validationFailures = getMetaModelContext().getSpecificationLoader().getOrAssessValidationResult();
            assertFalse(validationFailures.hasFailures());
        }
        @Test
        public void testValidationDomainObjectWithAliasesDefault() {
            metaModelContext = MetaModelContext_forTesting.builder()
                    .refiners(Can.of(__->facetFactory))
                    .build();
            facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
            ((MetaModelContext_forTesting) getMetaModelContext()).getProgrammingModel();//kicks off the programming model factory

            getMetaModelContext().getSpecificationLoader().loadSpecification(AliasDomainObjectWithAliases.class, IntrospectionState.FULLY_INTROSPECTED);
            ValidationFailures validationFailures = getMetaModelContext().getSpecificationLoader().getOrAssessValidationResult();
            assertTrue(validationFailures.hasFailures());
        }

        @Test
        public void testValidationDomainServiceWithAliasesDefault() {
            metaModelContext = MetaModelContext_forTesting.builder()
                    .refiners(Can.of(__->facetFactory))
                    .build();
            facetFactory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
            ((MetaModelContext_forTesting) getMetaModelContext()).getProgrammingModel();//kicks off the programming model factory

            getMetaModelContext().getSpecificationLoader().loadSpecification(AliasDomainServiceWithAliases.class, IntrospectionState.FULLY_INTROSPECTED);
            ValidationFailures validationFailures = getMetaModelContext().getSpecificationLoader().getOrAssessValidationResult();
            assertTrue(validationFailures.hasFailures());
        }
    }
}