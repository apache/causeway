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
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

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
        return serviceInjector.injectServicesInto(
                new ActionTester<T>(domainObjectType, actionName))
                .init();
    }

    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName) {
        return serviceInjector.injectServicesInto(
                new PropertyTester<T>(domainObjectType, propertyName))
                .init();
    }


    // -- ACTION TESTER

    public static class ActionTester<T> {

        @Inject private SpecificationLoader specificationLoader;
        @Inject private InteractionService interactionService;
        @Inject private FactoryService factoryService;

        @Getter private final Class<T> domainObjectType;
        @Getter private final String actionName;

        @Getter private ObjectSpecification objectSpecification;
        private Optional<ManagedAction> managedActionIfAny;

        private ActionTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String actionName) {

            this.domainObjectType = domainObjectType;
            this.actionName = actionName;
        }

        private ActionTester<T> init() {
            this.objectSpecification = specificationLoader.specForTypeElseFail(domainObjectType);
            val vm = ManagedObject.of(objectSpecification, factoryService.viewModel(domainObjectType));
            this.managedActionIfAny = ActionInteraction.start(vm, actionName, Where.NOT_SPECIFIED)
                    .getManagedAction();
            return this;
        }

        public void assertExists(final boolean isExpectedToExist) {

            if(isExpectedToExist
                    && managedActionIfAny.isEmpty()) {
                fail(String.format("action {} does not exist", actionName));
            }

            if(!isExpectedToExist
                    && managedActionIfAny.isPresent()) {
                fail(String.format("action {} does exist", actionName));
            }
        }

        public void assertVisibilityIsNotVetoed() {
            assertVisibilityIsVetoedWith(null);
        }

        public void assertVisibilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedVisible = expectedVetoReason == null;

            if(isExpectedVisible) {
                assertExists(true);
            }

            managedActionIfAny
            .ifPresent(managedAction->{
                interactionService.runAnonymous(()->{

                    final String actualVetoResaon = managedAction
                        .checkVisibility()
                        .map(veto->veto.getReason())
                        .orElse(null);

                    assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

        public void assertUsabilityIsNotVetoed() {
            assertUsabilityIsVetoedWith(null);
        }

        public void assertUsabilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedUsable = expectedVetoReason == null;

            if(isExpectedUsable) {
                assertExists(true);
            }

            managedActionIfAny
            .ifPresent(managedAction->{
                interactionService.runAnonymous(()->{
                    final String actualVetoResaon = managedAction
                            .checkUsability()
                            .map(veto->veto.getReason())
                            .orElse(null);

                        assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
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

    // -- PROPERTY/COLLECTION TESTER

    public static class PropertyTester<T> {

        @Inject private SpecificationLoader specificationLoader;
        @Inject private InteractionService interactionService;
        @Inject private FactoryService factoryService;

        @Getter private final Class<T> domainObjectType;
        @Getter private final String propertyName;

        @Getter private ObjectSpecification objectSpecification;
        private Optional<ManagedProperty> managedPropertyIfAny;

        private PropertyTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String propertyName) {

            this.domainObjectType = domainObjectType;
            this.propertyName = propertyName;
        }

        private PropertyTester<T> init() {
            this.objectSpecification = specificationLoader.specForTypeElseFail(domainObjectType);
            val vm = ManagedObject.of(objectSpecification, factoryService.viewModel(domainObjectType));
            this.managedPropertyIfAny = PropertyInteraction.start(vm, propertyName, Where.NOT_SPECIFIED)
                    .getManagedProperty();
            return this;
        }

        public void assertExists(final boolean isExpectedToExist) {

            if(isExpectedToExist
                    && managedPropertyIfAny.isEmpty()) {
                fail(String.format("action {} does not exist", propertyName));
            }

            if(!isExpectedToExist
                    && managedPropertyIfAny.isPresent()) {
                fail(String.format("action {} does exist", propertyName));
            }
        }

        public void assertVisibilityIsNotVetoed() {
            assertVisibilityIsVetoedWith(null);
        }

        public void assertVisibilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedVisible = expectedVetoReason == null;

            if(isExpectedVisible) {
                assertExists(true);
            }

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    final String actualVetoResaon = managedProperty
                        .checkVisibility()
                        .map(veto->veto.getReason())
                        .orElse(null);

                    assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

        public void assertUsabilityIsNotVetoed() {
            assertUsabilityIsVetoedWith(null);
        }

        public void assertUsabilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedUsable = expectedVetoReason == null;

            if(isExpectedUsable) {
                assertExists(true);
            }

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{
                    final String actualVetoResaon = managedProperty
                            .checkUsability()
                            .map(veto->veto.getReason())
                            .orElse(null);

                        assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
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

}