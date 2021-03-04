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

package org.apache.isis.core.metamodel.facets.object.domainobject;

import java.util.UUID;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.EditingObjectsConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory.ProcessObjectSpecIdContext;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;

import lombok.val;

public class DomainObjectAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    DomainObjectAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new DomainObjectAnnotationFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
    }


    class SomeHasInteractionId implements HasInteractionId {

        @Override
        public UUID getInteractionId() {
            return null;
        }

    }

    void allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy value) {
        if(value!=null) {
            val config = super.metaModelContext.getConfiguration();
            config.getApplib().getAnnotation().getDomainObject().setEntityChangePublishing(value);
        }
    }

    void allowingObjectsEditingToReturn(EditingObjectsConfiguration value) {
        if(value!=null) {
            final IsisConfiguration config = super.metaModelContext.getConfiguration();
            config.getApplib().getAnnotation().getDomainObject().setEditing(value);
        }
    }

    protected void ignoringConfiguration() {

    }

    public static class EntityChangePublishing extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(entityChangePublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
        class CustomerWithDomainObjectAndAuditingSetToAsConfigured {
        }

        @DomainObject(entityChangePublishing = org.apache.isis.applib.annotation.Publishing.DISABLED)
        class CustomerWithDomainObjectAndAuditingSetToDisabled {
        }

        @DomainObject(entityChangePublishing = org.apache.isis.applib.annotation.Publishing.ENABLED)
        class CustomerWithDomainObjectAndAuditingSetToEnabled {
        }

        @Test
        public void ignore_HasUniqueId() {

            allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.ALL);

            facetFactory.processEntityChangePublishing(new ProcessClassContext(HasInteractionId.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        public static class WhenNotAnnotatedAndDefaultsFromConfiguration extends EntityChangePublishing {

            @Test
            public void configured_value_set_to_all() {
                allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.ALL);

                facetFactory.processEntityChangePublishing(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                assertThat(facet, is(notNullValue()));
                Assert.assertTrue(facet instanceof EntityChangePublishingFacetFromConfiguration);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.NONE);

                facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

                final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithAuditingSetToAsConfigured extends EntityChangePublishing {

            @Test
            public void configured_value_set_to_all() {
                allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.ALL);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof EntityChangePublishingFacetForDomainObjectAnnotationAsConfigured);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.NONE);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, mockMethodRemover, facetHolder));

                final EntityChangePublishingFacet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithAuditingSetToEnabled extends EntityChangePublishing {

            @Test
            public void irrespective_of_configured_value() {
                allowingEntityChangePublishingToReturn(null);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToEnabled.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(EntityChangePublishingFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof EntityChangePublishingFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithAuditingSetToDisabled extends EntityChangePublishing {

            @Test
            public void irrespective_of_configured_value() {
                allowingEntityChangePublishingToReturn(PublishingPolicies.EntityChangePublishingPolicy.ALL);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToDisabled.class, mockMethodRemover, facetHolder));

                assertFalse(EntityChangePublishingFacet.isPublishingEnabled(facetHolder));

                expectNoMethodsRemoved();
            }

        }

    }

    public static class AutoComplete extends DomainObjectAnnotationFacetFactoryTest {

        class CustomerRepository {
            public String lookup(final String x) { return null; }
        }

        class CustomerRepositoryWithDefaultMethodName {
            public String autoComplete(final String x) { return null; }
        }

        @DomainObject(autoCompleteRepository = CustomerRepository.class, autoCompleteAction = "lookup")
        class CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction {
        }

        @DomainObject(autoCompleteRepository = CustomerRepositoryWithDefaultMethodName.class)
        class CustomerWithDomainObjectAndAutoCompleteRepository {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoAutoCompleteRepository {
        }

        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();

            context.checking(new Expectations() {
                {
                    //                    allowing(mockServicesInjector).isRegisteredService(CustomerRepository.class);
                    //                    will(returnValue(true));
                    //
                    //                    allowing(mockServicesInjector).isRegisteredService(CustomerRepositoryWithDefaultMethodName.class);
                    //                    will(returnValue(true));

                    // anything else

                }
            });

        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepositoryAndAction() {

            facetFactory.process(new ProcessClassContext(
                    CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof AutoCompleteFacetForDomainObjectAnnotation);

            final AutoCompleteFacetForDomainObjectAnnotation autoCompleteFacet = (AutoCompleteFacetForDomainObjectAnnotation) facet;

            assertThat(CustomerRepository.class.isAssignableFrom(autoCompleteFacet.getRepositoryClass()), is(true));
            assertThat(autoCompleteFacet.getActionName(), is("lookup"));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepository() {

            facetFactory.process(new ProcessClassContext(
                    CustomerWithDomainObjectAndAutoCompleteRepository.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof AutoCompleteFacetForDomainObjectAnnotation);

            final AutoCompleteFacetForDomainObjectAnnotation autoCompleteFacet = (AutoCompleteFacetForDomainObjectAnnotation) facet;

            assertThat(CustomerRepositoryWithDefaultMethodName.class.isAssignableFrom(autoCompleteFacet.getRepositoryClass()), is(true));
            assertThat(autoCompleteFacet.getActionName(), is("autoComplete"));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAnnotationButNoAutoComplete() {

            facetFactory.process(new ProcessClassContext(
                    CustomerWithDomainObjectButNoAutoCompleteRepository.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(
                    DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Bounded extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(bounding = Bounding.BOUNDED)
        class CustomerWithDomainObjectAndBoundedSetToTrue {
        }

        @DomainObject(bounding = Bounding.UNBOUNDED)
        class CustomerWithDomainObjectAndBoundedSetToFalse {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoBounded {
        }

        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndBoundedSetToTrue() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndBoundedSetToTrue.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof ChoicesFacetForDomainObjectAnnotation);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepository() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndBoundedSetToFalse.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Editing extends DomainObjectAnnotationFacetFactoryTest {

        class CustomerWithImmutableAnnotation {
        }

        @DomainObject(editing = org.apache.isis.applib.annotation.Editing.AS_CONFIGURED)
        class CustomerWithDomainObjectAndEditingSetToAsConfigured {
        }

        @DomainObject(editing = org.apache.isis.applib.annotation.Editing.DISABLED)
        class CustomerWithDomainObjectAndEditingSetToDisabled {
        }

        @DomainObject(editing = org.apache.isis.applib.annotation.Editing.ENABLED)
        class CustomerWithDomainObjectAndEditingSetToEnabled {
        }


        public static class WhenNotAnnotatedAndDefaultsFromConfiguration extends Editing {

            @Test
            public void configured_value_set_to_true() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.TRUE);

                facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_false() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.FALSE);

                facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetFromConfiguration);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_defaults() {
                //allowingConfigurationToReturn("isis.objects.editing", "foobar");

                facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet); // default is now non-editable
                Assert.assertTrue(facet instanceof ImmutableFacetFromConfiguration);


                expectNoMethodsRemoved();
            }
        }


        public static class WithDomainObjectAnnotationWithEditingSetToAsConfigured extends Editing {

            @Test
            public void configured_value_set_to_true() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.TRUE);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_false() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.FALSE);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_defaults() {
                //allowingConfigurationToReturn("isis.objects.editing", "foobar");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet); // default is now non-editable
                Assert.assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotationAsConfigured);

                expectNoMethodsRemoved();
            }
        }

        public static class WithDomainObjectAnnotationWithEditingSetToEnabled extends Editing {

            @Test
            public void irrespective_of_configured_value() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.FALSE);

                facetFactory.process(new ProcessClassContext(
                        CustomerWithDomainObjectAndEditingSetToEnabled.class, mockMethodRemover, facetHolder));

                final ImmutableFacet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithEditingSetToDisabled extends Editing {

            @Test
            public void irrespective_of_configured_value() {
                allowingObjectsEditingToReturn(EditingObjectsConfiguration.TRUE);

                facetFactory.process(new ProcessClassContext(
                        CustomerWithDomainObjectAndEditingSetToDisabled.class, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

        }

    }

    public static class ObjectType extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(objectType = "CUS")
        class CustomerWithDomainObjectAndObjectTypeSet {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoObjectType {
        }

        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndObjectTypeSetToTrue() {

            facetFactory.process(new ProcessObjectSpecIdContext(CustomerWithDomainObjectAndObjectTypeSet.class, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof ObjectSpecIdFacetForDomainObjectAnnotation);
            final ObjectSpecIdFacetForDomainObjectAnnotation facetForDomainObjectAnnotation =
                    (ObjectSpecIdFacetForDomainObjectAnnotation) facet;

            assertThat(facetForDomainObjectAnnotation.value(), is("CUS"));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndObjectTypeNotSet() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectButNoObjectType.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Nature extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.ENTITY)
        class CustomerWithDomainObjectAndNatureSetToJdoEntity {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.NOT_SPECIFIED)
        class CustomerWithDomainObjectAndNatureSetToNotSpecified {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.VIEW_MODEL)
        class CustomerWithDomainObjectAndNatureSetToViewModel {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoNature {
        }

        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndNatureSetToJdoEntity() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToJdoEntity.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndNatureSetToNotSpecified() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToNotSpecified.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndNatureSetToViewModel() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToViewModel.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof RecreatableObjectFacetForDomainObjectAnnotation);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(DomainObjectAnnotationFacetFactoryTest.Customer.class, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }


}
