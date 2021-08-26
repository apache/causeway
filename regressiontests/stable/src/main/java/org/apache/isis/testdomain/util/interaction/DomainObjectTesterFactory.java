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
package org.apache.isis.testdomain.util.interaction;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.util.CollectionAssertions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
public class DomainObjectTesterFactory {

    private final @NonNull ServiceInjector serviceInjector;

    public <T> ActionTester<T> actionTester(
            final Class<T> domainObjectType,
            final String actionName) {
        val tester = serviceInjector.injectServicesInto(
                new ActionTester<T>(domainObjectType, actionName));
        tester.init();
        return tester;
    }

    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName) {
        val tester = serviceInjector.injectServicesInto(
                new PropertyTester<T>(domainObjectType, propertyName));
        tester.init();
        return tester;
    }

    public <T> CollectionTester<T> collectionTester(
            final Class<T> domainObjectType,
            final String collectionName) {
        val tester = serviceInjector.injectServicesInto(
                new CollectionTester<T>(domainObjectType, collectionName));
        tester.init();
        return tester;
    }


    // -- ACTION TESTER

    public static class ActionTester<T>
    extends MemberTester<T> {

        private Optional<ManagedAction> managedActionIfAny;

        private ActionTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String actionName) {
            super(domainObjectType, actionName, "actionName");
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedActionIfAny = ActionInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedAction();
        }

        /**
         * circumvents rule checking
         */
        public void assertInvocationResult(
                final @Nullable Object expectedResult,
                final @Nullable List<Object> pojoArgList) {

            assertExists(true);

            managedActionIfAny
            .ifPresent(managedAction->{
                interactionService.runAnonymous(()->{

                    val args = managedAction.getInteractionHead()
                            .getPopulatedParameterValues(pojoArgList);

                    // spawns its own transactional boundary, or reuses an existing one if available
                    val either = managedAction.invoke(args);

                    assertTrue(either.isLeft()); // assert action did not throw

                    val actionResultAsPojo = either.leftIfAny().getPojo();

                    assertEquals(expectedResult, actionResultAsPojo);

                });
            });

        }

    }

    // -- PROPERTY TESTER

    public static class PropertyTester<T>
    extends MemberTester<T> {

        private Optional<ManagedProperty> managedPropertyIfAny;

        private PropertyTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String propertyName) {
            super(domainObjectType, propertyName, "property");
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedPropertyIfAny = PropertyInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedProperty();
        }

        /**
         * circumvents rule checking
         */
        public void assertValue(final Object expectedPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{
                    assertEquals(expectedPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });
        }

        /**
         * circumvents rule checking
         */
        public void assertValueUpdate(final Object proposedNewPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    val newPropertyValue = managedProperty.getMetaModel().getMetaModelContext()
                          .getObjectManager().adapt(proposedNewPropertyValue);

                    managedProperty.modifyProperty(newPropertyValue);
                    assertEquals(proposedNewPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });

        }

    }

    // -- COLLECTION TESTER

    public static class CollectionTester<T>
    extends MemberTester<T> {

        private Optional<ManagedCollection> managedCollectionIfAny;

        private CollectionTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String collectionName) {
            super(domainObjectType, collectionName, "collection");
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedCollectionIfAny = CollectionInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedCollection();
        }

        /**
         * circumvents rule checking
         */
        public void assertCollectionElements(final Iterable<?> expectedCollectionElements) {

            assertExists(true);

            managedCollectionIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    CollectionAssertions
                    .assertComponentWiseEquals(expectedCollectionElements, managedCollection.getCollectionValue().getPojo());
                });
            });
        }

    }

    // -- COMMON ABSTRACT MEMBER TESTER

    private static abstract class MemberTester<T> {

        @Inject protected SpecificationLoader specificationLoader;
        @Inject protected InteractionService interactionService;
        @Inject protected FactoryService factoryService;

        @Getter private final Class<T> domainObjectType;
        @Getter private final String memberName;
        private final String memberSort;

        @Getter private ObjectSpecification objectSpecification;
        private Optional<? extends ManagedMember> managedMemberIfAny;

        protected MemberTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String memberName,
                final @NonNull String memberSort) {

            this.domainObjectType = domainObjectType;
            this.memberName = memberName;
            this.memberSort = memberSort;
        }

        protected final MemberTester<T> init() {
            this.objectSpecification = specificationLoader.specForTypeElseFail(domainObjectType);
            val vm = ManagedObject.of(objectSpecification, factoryService.viewModel(domainObjectType));
            this.managedMemberIfAny = startInteractionOn(vm);
            return this;
        }

        protected abstract Optional<? extends ManagedMember> startInteractionOn(ManagedObject viewModel);


        public final void assertExists(final boolean isExpectedToExist) {

            if(isExpectedToExist
                    && managedMemberIfAny.isEmpty()) {
                fail(String.format("{} {} does not exist", memberSort, memberName));
            }

            if(!isExpectedToExist
                    && managedMemberIfAny.isPresent()) {
                fail(String.format("{} {} does exist", memberSort, memberName));
            }
        }

        public final void assertVisibilityIsNotVetoed() {
            assertVisibilityIsVetoedWith(null);
        }

        public final void assertVisibilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedVisible = expectedVetoReason == null;

            if(isExpectedVisible) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{

                    final String actualVetoResaon = managedCollection
                        .checkVisibility()
                        .map(veto->veto.getReason())
                        .orElse(null);

                    assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

        public final void assertUsabilityIsNotVetoed() {
            assertUsabilityIsVetoedWith(null);
        }

        public final void assertUsabilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedUsable = expectedVetoReason == null;

            if(isExpectedUsable) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    final String actualVetoResaon = managedCollection
                            .checkUsability()
                            .map(veto->veto.getReason())
                            .orElse(null);

                        assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

    }


}