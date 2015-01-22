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
import org.apache.isis.applib.annotation.Audited;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForObject;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForAuditedAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForDomainObjectAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForDomainObjectAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForPublishedObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.ImmutableFacetForImmutableAnnotation;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propparam.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class DomainObjectAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    DomainObjectAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new DomainObjectAnnotationFacetFactory();
        facetFactory.setConfiguration(mockConfiguration);
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
    }


    class SomeTransactionalId implements HasTransactionId {

        @Override
        public UUID getTransactionId() {
            return null;
        }

        @Override
        public void setTransactionId(final UUID transactionId) {
        }
    }

    protected void allowingConfigurationToReturn(final String name, final String value) {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString(name);
                will(returnValue(value));

                // anything else
                ignoring(mockConfiguration);
            }
        });
    }

    protected void ignoringConfiguration() {
        context.checking(new Expectations() {
            {
                ignoring(mockConfiguration);
            }
        });
    }


    public static class Auditing extends DomainObjectAnnotationFacetFactoryTest {

        @Audited
        class CustomerWithAuditedAnnotation {
        }

        @DomainObject(auditing = org.apache.isis.applib.annotation.Auditing.AS_CONFIGURED)
        class CustomerWithDomainObjectAndAuditingSetToAsConfigured {
        }

        @DomainObject(auditing = org.apache.isis.applib.annotation.Auditing.DISABLED)
        class CustomerWithDomainObjectAndAuditingSetToDisabled {
        }

        @DomainObject(auditing = org.apache.isis.applib.annotation.Auditing.ENABLED)
        class CustomerWithDomainObjectAndAuditingSetToEnabled {
        }

        @Test
        public void ignoreHasTransactionId() {

            allowingConfigurationToReturn("isis.services.audit.objects", "all");

            facetFactory.processAuditing(new ProcessClassContext(HasTransactionId.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AuditableFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        public static class WhenNotAnnotatedAndDefaultsFromConfiguration extends Auditing {

            @Test
            public void configured_value_set_to_all() {
                allowingConfigurationToReturn("isis.services.audit.objects", "all");

                facetFactory.processAuditing(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                assertThat(facet, is(notNullValue()));
                Assert.assertTrue(facet instanceof AuditableFacetFromConfiguration);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingConfigurationToReturn("isis.services.audit.objects", "none");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.services.audit.objects", "foobar");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

        public static class WithAuditedAnnotation extends Auditing {

            @Before
            public void setUp() throws Exception {
                super.setUp();
                allowingConfigurationToReturn("isis.services.audit.objects", null);
            }

            @Test
            public void has_annotation() {

                facetFactory.process(new ProcessClassContext(CustomerWithAuditedAnnotation.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof AuditableFacetForAuditedAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void does_not_have_annotation() {

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithAuditingSetToAsConfigured extends Auditing {

            @Test
            public void configured_value_set_to_all() {
                allowingConfigurationToReturn("isis.services.audit.objects", "all");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof AuditableFacetForDomainObjectAnnotationAsConfigured);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingConfigurationToReturn("isis.services.audit.objects", "none");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.services.audit.objects", "foobar");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

        public static class WithDomainObjectAnnotationWithAuditingSetToEnabled extends Auditing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.services.audit.objects", null);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToEnabled.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof AuditableFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithAuditingSetToDisabled extends Auditing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.services.audit.objects", "all");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAuditingSetToDisabled.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

    }

    public static class Publishing extends DomainObjectAnnotationFacetFactoryTest {

        @PublishedObject
        class CustomerWithPublishedObjectAnnotation {
        }

        @DomainObject(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
        class CustomerWithDomainObjectAndPublishingSetToAsConfigured {
        }

        @DomainObject(publishing = org.apache.isis.applib.annotation.Publishing.DISABLED)
        class CustomerWithDomainObjectAndPublishingSetToDisabled {
        }

        @DomainObject(publishing = org.apache.isis.applib.annotation.Publishing.ENABLED)
        class CustomerWithDomainObjectAndPublishingSetToEnabled {
        }

        public static class CustomerPayloadFactory implements PublishingPayloadFactoryForObject {
            @Override
            public EventPayload payloadFor(final Object changedObject, final PublishingChangeKind publishingChangeKind) {
                return null;
            }
        }

        @DomainObject(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED, publishingPayloadFactory = CustomerPayloadFactory.class)
        class CustomerWithDomainObjectAndPublishingSetToAsConfiguredWithCustomPayloadFactory {
        }

        @DomainObject(publishing = org.apache.isis.applib.annotation.Publishing.ENABLED, publishingPayloadFactory = CustomerPayloadFactory.class)
        class CustomerWithDomainObjectAndPublishingSetToEnabledWithCustomPayloadFactory {
        }

        @Test
        public void ignoreHasTransactionId() {

            allowingConfigurationToReturn("isis.services.publish.objects", "all");

            facetFactory.process(new ProcessClassContext(HasTransactionId.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        public static class WhenNotAnnotatedAndDefaultsFromConfiguration extends Publishing {

            @Test
            public void configured_value_set_to_all() {
                allowingConfigurationToReturn("isis.services.publish.objects", "all");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetFromConfiguration);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingConfigurationToReturn("isis.services.publish.objects", "none");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.services.publish.objects", "foobar");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

        public static class WithPublishedObjectAnnotation extends Publishing {

            @Before
            public void setUp() throws Exception {
                super.setUp();
                allowingConfigurationToReturn("isis.services.publish.objects", null);
            }

            @Test
            public void has_annotation() {

                facetFactory.process(new ProcessClassContext(CustomerWithPublishedObjectAnnotation.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetForPublishedObjectAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void does_not_have_annotation() {

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithPublishingSetToAsConfigured extends Publishing {

            @Test
            public void configured_value_set_to_all() {
                allowingConfigurationToReturn("isis.services.publish.objects", "all");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetForDomainObjectAnnotationAsConfigured);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_all_forCustomPayloadFactory() {
                allowingConfigurationToReturn("isis.services.publish.objects", "all");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToAsConfiguredWithCustomPayloadFactory.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetForDomainObjectAnnotationAsConfigured);

                final PublishedObject.PayloadFactory payloadFactory = ((PublishedObjectFacetForDomainObjectAnnotationAsConfigured) facet).value();

                assertThat(payloadFactory, instanceOf(PublishedObjectFacetAbstract.LegacyAdapter.class));
                final PublishedObjectFacetAbstract.LegacyAdapter legacyAdapter = (PublishedObjectFacetAbstract.LegacyAdapter) payloadFactory;

                final PublishingPayloadFactoryForObject specifiedPayloadFactory = legacyAdapter.getPayloadFactory();
                assertThat(specifiedPayloadFactory, instanceOf(CustomerPayloadFactory.class));

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_none() {
                allowingConfigurationToReturn("isis.services.publish.objects", "none");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.services.publish.objects", "foobar");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithPublishingSetToEnabled extends Publishing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.services.publish.objects", null);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToEnabled.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void irrespective_of_configured_value_forCustomPayloadFactory() {
                allowingConfigurationToReturn("isis.services.publish.objects", null);

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToEnabledWithCustomPayloadFactory.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(PublishedObjectFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof PublishedObjectFacetForDomainObjectAnnotation);

                final PublishedObject.PayloadFactory payloadFactory = ((PublishedObjectFacetForDomainObjectAnnotation) facet).value();

                assertThat(payloadFactory, instanceOf(PublishedObjectFacetForDomainObjectAnnotation.LegacyAdapter.class));
                final PublishedObjectFacetForDomainObjectAnnotation.LegacyAdapter legacyAdapter = (PublishedObjectFacetForDomainObjectAnnotation.LegacyAdapter) payloadFactory;

                final PublishingPayloadFactoryForObject specifiedPayloadFactory = legacyAdapter.getPayloadFactory();
                assertThat(specifiedPayloadFactory, instanceOf(CustomerPayloadFactory.class));

                expectNoMethodsRemoved();
            }
        }

        public static class WithDomainObjectAnnotationWithPublishingSetToDisabled extends Publishing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.services.publish.objects", "all");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndPublishingSetToDisabled.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(AuditableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }
    }

    public static class AutoComplete extends DomainObjectAnnotationFacetFactoryTest {

        class CustomerRepository {
            public String lookup(final String x) { return null; }
        }

        @DomainObject(autoCompleteRepository = CustomerRepository.class, autoCompleteAction = "lookup")
        class CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction {
        }

        @DomainObject(autoCompleteRepository = CustomerRepository.class)
        class CustomerWithDomainObjectAndAutoCompleteRepository {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoAutoCompleteRepository {
        }

        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepositoryAndAction() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAutoCompleteRepositoryAndAction.class, null, mockMethodRemover, facetHolder));

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

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndAutoCompleteRepository.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof AutoCompleteFacetForDomainObjectAnnotation);

            final AutoCompleteFacetForDomainObjectAnnotation autoCompleteFacet = (AutoCompleteFacetForDomainObjectAnnotation) facet;

            assertThat(CustomerRepository.class.isAssignableFrom(autoCompleteFacet.getRepositoryClass()), is(true));
            assertThat(autoCompleteFacet.getActionName(), is("autoComplete"));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAnnotationButNoAutoComplete() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectButNoAutoCompleteRepository.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(AutoCompleteFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Bounded extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(bounded = true)
        class CustomerWithDomainObjectAndBoundedSetToTrue {
        }

        @DomainObject(bounded = false)
        class CustomerWithDomainObjectAndBoundedSetToFalse {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoBounded {
        }

        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndBoundedSetToTrue() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndBoundedSetToTrue.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof ChoicesFacetForDomainObjectAnnotation);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndAutoCompleteRepository() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndBoundedSetToFalse.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ChoicesFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Editing extends DomainObjectAnnotationFacetFactoryTest {

        @Immutable
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
                allowingConfigurationToReturn("isis.objects.editing", "true");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_false() {
                allowingConfigurationToReturn("isis.objects.editing", "false");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetFromConfiguration);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.objects.editing", "foobar");

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

        public static class WithImmutableAnnotation extends Editing {

            @Before
            public void setUp() throws Exception {
                super.setUp();
                ignoringConfiguration();
            }

            @Test
            public void has_annotation() {

                final ProcessClassContext processClassContext = new ProcessClassContext(CustomerWithImmutableAnnotation.class, null, mockMethodRemover, facetHolder);
                facetFactory.process(processClassContext);

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetForImmutableAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void does_not_have_annotation() {

                facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithEditingSetToAsConfigured extends Editing {

            @Test
            public void configured_value_set_to_true() {
                allowingConfigurationToReturn("isis.objects.editing", "true");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_false() {
                allowingConfigurationToReturn("isis.objects.editing", "false");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNotNull(facet);
                Assert.assertTrue(facet instanceof ImmutableFacetForDomainObjectAnnotation);

                expectNoMethodsRemoved();
            }

            @Test
            public void configured_value_set_to_not_recognized() {
                allowingConfigurationToReturn("isis.objects.editing", "foobar");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToAsConfigured.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }
        }

        public static class WithDomainObjectAnnotationWithEditingSetToEnabled extends Editing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.objects.editing", "false");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToEnabled.class, null, mockMethodRemover, facetHolder));

                final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
                Assert.assertNull(facet);

                expectNoMethodsRemoved();
            }

        }

        public static class WithDomainObjectAnnotationWithEditingSetToDisabled extends Editing {

            @Test
            public void irrespective_of_configured_value() {
                allowingConfigurationToReturn("isis.objects.editing", "true");

                facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndEditingSetToDisabled.class, null, mockMethodRemover, facetHolder));

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

        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndObjectTypeSetToTrue() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndObjectTypeSet.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof ObjectSpecIdFacetForDomainObjectAnnotation);
            final ObjectSpecIdFacetForDomainObjectAnnotation facetForDomainObjectAnnotation =
                    (ObjectSpecIdFacetForDomainObjectAnnotation) facet;

            assertThat(facetForDomainObjectAnnotation.value(), is(ObjectSpecId.of("CUS")));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndObjectTypeNotSet() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectButNoObjectType.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ObjectSpecIdFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }

    public static class Nature extends DomainObjectAnnotationFacetFactoryTest {

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.JDO_ENTITY)
        class CustomerWithDomainObjectAndNatureSetToJdoEntity {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.NOT_SPECIFIED)
        class CustomerWithDomainObjectAndNatureSetToNotSpecified {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.INMEMORY_ENTITY)
        class CustomerWithDomainObjectAndNatureSetToInmemoryEntity {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.EXTERNAL_ENTITY)
        class CustomerWithDomainObjectAndNatureSetToExternalEntity {
        }

        @DomainObject(nature = org.apache.isis.applib.annotation.Nature.VIEW_MODEL)
        class CustomerWithDomainObjectAndNatureSetToViewModel {
        }

        @DomainObject
        class CustomerWithDomainObjectButNoNature {
        }

        @Before
        public void setUp() throws Exception {
            super.setUp();
            ignoringConfiguration();
        }

        @Test
        public void whenDomainObjectAndNatureSetToJdoEntity() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToJdoEntity.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndNatureSetToNotSpecified() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToNotSpecified.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndNatureSetToViewModel() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToViewModel.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof RecreatableObjectFacetForDomainObjectAnnotation);
            final RecreatableObjectFacetForDomainObjectAnnotation facetForDomainObjectAnnotation =
                    (RecreatableObjectFacetForDomainObjectAnnotation) facet;

            assertThat(facetForDomainObjectAnnotation.getArchitecturalLayer(), is(ViewModelFacet.ArchitecturalLayer.APPLICATION));

            expectNoMethodsRemoved();
        }


        @Test
        public void whenDomainObjectAndNatureSetToExternalEntity() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToExternalEntity.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof RecreatableObjectFacetForDomainObjectAnnotation);
            final RecreatableObjectFacetForDomainObjectAnnotation facetForDomainObjectAnnotation =
                    (RecreatableObjectFacetForDomainObjectAnnotation) facet;

            assertThat(facetForDomainObjectAnnotation.getArchitecturalLayer(), is(ViewModelFacet.ArchitecturalLayer.DOMAIN));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenDomainObjectAndNatureSetToInmemoryEntity() {

            facetFactory.process(new ProcessClassContext(CustomerWithDomainObjectAndNatureSetToInmemoryEntity.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNotNull(facet);

            Assert.assertTrue(facet instanceof RecreatableObjectFacetForDomainObjectAnnotation);
            final RecreatableObjectFacetForDomainObjectAnnotation facetForDomainObjectAnnotation =
                    (RecreatableObjectFacetForDomainObjectAnnotation) facet;

            assertThat(facetForDomainObjectAnnotation.getArchitecturalLayer(), is(ViewModelFacet.ArchitecturalLayer.DOMAIN));

            expectNoMethodsRemoved();
        }

        @Test
        public void whenNoDomainObjectAnnotation() {

            facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetHolder));

            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            Assert.assertNull(facet);

            expectNoMethodsRemoved();
        }

    }


}