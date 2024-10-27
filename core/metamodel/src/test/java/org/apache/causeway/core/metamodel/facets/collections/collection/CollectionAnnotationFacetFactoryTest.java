/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.collections.collection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.commons.matchers.CausewayMatchers;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacetFromFeature;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.typeof.TypeOfFacetForCollectionAnnotation;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.postprocessors.members.SynthesizeDomainEventsForMixinPostProcessor;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

class CollectionAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    CollectionAnnotationFacetFactory facetFactory;

    private static void processDomainEvent(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        var collectionIfAny = facetFactory.collectionIfAny(processMethodContext);
        facetFactory.processDomainEvent(processMethodContext, collectionIfAny);
    }

    private static void processTypeOf(
            final CollectionAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        var collectionIfAny = facetFactory.collectionIfAny(processMethodContext);
        facetFactory.processTypeOf(processMethodContext, collectionIfAny);
    }

    @BeforeEach
    public void setUp() throws Exception {
        facetFactory = new CollectionAnnotationFacetFactory(getMetaModelContext());
    }

    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    @TestInstance(Lifecycle.PER_CLASS)
    static class DomainEventTests extends CollectionAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            var mockOnType = Mockito.mock(ObjectSpecification.class);
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(mockOnType, holder) {
                @Override
                public Object getProperty(
                        final ManagedObject inObject,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return null;
                }
            });
        }

        private void assertHasCollectionDomainEventFacet(
                final FacetedMethod facetedMethod,
                final EventTypeOrigin eventTypeOrigin,
                final Class<? extends CollectionDomainEvent<?,?>> eventType) {
            var domainEventFacet = facetedMethod.lookupFacet(CollectionDomainEventFacet.class).orElseThrow();
            assertEquals(eventTypeOrigin, domainEventFacet.getEventTypeOrigin());
            assertThat(domainEventFacet.getEventType(), CausewayMatchers.classEqualTo(eventType));
        }

        @Test
        void withCollectionDomainEvent_fallingBackToDefault() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                @Getter @Setter private List<Order> orders;
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                addGetterFacet(facetedMethod);

                // when
                processDomainEvent(facetFactory, processMethodContext);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.DEFAULT, CollectionDomainEvent.Default.class);
            });
        }

        @Test
        void withCollectionDomainEvent_annotatedOnMethod() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent extends CollectionDomainEvent<Customer, String> {}
                @Collection(domainEvent = OrdersShowingDomainEvent.class)
                @Getter @Setter private List<Order> orders;
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                addGetterFacet(facetedMethod);

                // when
                processDomainEvent(facetFactory, processMethodContext);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent.class);
            });
        }

        @Test
        void withCollectionDomainEvent_annotatedOnType() {

            class Order {
            }
            @DomainObject(collectionDomainEvent = Customer.OrdersShowingDomainEvent.class)
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent extends CollectionDomainEvent<Customer, String> {}
                @Collection
                @Getter @Setter private List<Order> orders;
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                addGetterFacet(facetedMethod);

                // when
                processDomainEvent(facetFactory, processMethodContext);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_OBJECT, Customer.OrdersShowingDomainEvent.class);
            });
        }

        @Test
        void withCollectionDomainEvent_annotatedOnTypeAndMethod() {

            class Order {
            }
            @DomainObject(collectionDomainEvent = Customer.OrdersShowingDomainEvent1.class)
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent1 extends CollectionDomainEvent<Customer, String> {}
                class OrdersShowingDomainEvent2 extends CollectionDomainEvent<Customer, String> {}
                @Collection(domainEvent = OrdersShowingDomainEvent2.class)
                @Getter @Setter private List<Order> orders;
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                addGetterFacet(facetedMethod);

                // when
                processDomainEvent(facetFactory, processMethodContext);

                // then - the collection annotation should win
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent2.class);
            });
        }

        @Test
        void withCollectionDomainEvent_mixedIn_annotatedOnMethod() {
            var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent extends CollectionDomainEvent<Customer, String> {}
                @Getter @Setter private List<Order> orders;
            }
            @DomainObject(nature=Nature.MIXIN, mixinMethod = "coll")
            @RequiredArgsConstructor
            @SuppressWarnings("unused")
            class Customer_orders {
                final Customer mixee;
                @Collection(domainEvent = Customer.OrdersShowingDomainEvent.class)
                public List<Order> coll() { return Collections.emptyList(); }
            }

            collectionScenarioMixedIn(Customer.class, Customer_orders.class,
                    (processMethodContext, mixeeSpec, facetedMethod, mixedInColl)->{

                // when
                processDomainEvent(facetFactory, processMethodContext);
                postProcessor.postProcessCollection(mixeeSpec, mixedInColl);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent.class);
            });
        }

        @Test
        void withCollectionDomainEvent_mixedIn_annotatedOnMixedInType() {
            var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent extends CollectionDomainEvent<Customer, String> {}
                @Getter @Setter private List<Order> orders;
            }
            @Collection(domainEvent = Customer.OrdersShowingDomainEvent.class)
            @RequiredArgsConstructor
            @SuppressWarnings("unused")
            class Customer_orders {
                final Customer mixee;
                @MemberSupport
                public List<Order> coll() { return Collections.emptyList(); }
            }

            collectionScenarioMixedIn(Customer.class, Customer_orders.class,
                    (processMethodContext, mixeeSpec, facetedMethod, mixedInColl)->{

                // when
                processDomainEvent(facetFactory, processMethodContext);
                postProcessor.postProcessCollection(mixeeSpec, mixedInColl);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent.class);
            });
        }

        @Test
        void withCollectionDomainEvent_mixedIn_annotatedOnMixeeType() {
            var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

            class Order {
            }
            @DomainObject(collectionDomainEvent = Customer.OrdersShowingDomainEvent.class)
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent extends CollectionDomainEvent<Customer, String> {}
                @Getter @Setter private List<Order> orders;
            }
            @Collection
            @RequiredArgsConstructor
            @SuppressWarnings("unused")
            class Customer_orders {
                final Customer mixee;
                @MemberSupport
                public List<Order> coll() { return Collections.emptyList(); }
            }

            collectionScenarioMixedIn(Customer.class, Customer_orders.class,
                    (processMethodContext, mixeeSpec, facetedMethod, mixedInColl)->{

                // when
                processDomainEvent(facetFactory, processMethodContext);
                postProcessor.postProcessCollection(mixeeSpec, mixedInColl);

                // then
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_OBJECT, Customer.OrdersShowingDomainEvent.class);
            });
        }

        @Test
        void withCollectionDomainEvent_mixedIn_annotatedOnMixeeAndMixedInType() {
            var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

            class Order {
            }
            @DomainObject(collectionDomainEvent = Customer.OrdersShowingDomainEvent1.class)
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent1 extends CollectionDomainEvent<Customer, String> {}
                class OrdersShowingDomainEvent2 extends CollectionDomainEvent<Customer, String> {}
                @Getter @Setter private List<Order> orders;
            }
            @Collection(domainEvent = Customer.OrdersShowingDomainEvent2.class)
            @RequiredArgsConstructor
            @SuppressWarnings("unused")
            class Customer_orders {
                final Customer mixee;
                @MemberSupport
                public List<Order> coll() { return Collections.emptyList(); }
            }

            collectionScenarioMixedIn(Customer.class, Customer_orders.class,
                    (processMethodContext, mixeeSpec, facetedMethod, mixedInColl)->{

                // when
                processDomainEvent(facetFactory, processMethodContext);
                postProcessor.postProcessCollection(mixeeSpec, mixedInColl);

                // then - the mixed-in annotation should win
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent2.class);
            });
        }

        @Test
        void withCollectionDomainEvent_mixedIn_annotatedOnMixeeTypeAndMixedInMethod() {
            var postProcessor = new SynthesizeDomainEventsForMixinPostProcessor(getMetaModelContext());

            class Order {
            }
            @DomainObject(collectionDomainEvent = Customer.OrdersShowingDomainEvent1.class)
            @SuppressWarnings("unused")
            class Customer {
                class OrdersShowingDomainEvent1 extends CollectionDomainEvent<Customer, String> {}
                class OrdersShowingDomainEvent2 extends CollectionDomainEvent<Customer, String> {}
                @Getter @Setter private List<Order> orders;
            }
            @DomainObject(nature=Nature.MIXIN, mixinMethod = "coll")
            @RequiredArgsConstructor
            @SuppressWarnings("unused")
            class Customer_orders {
                final Customer mixee;
                @Collection(domainEvent = Customer.OrdersShowingDomainEvent2.class)
                public List<Order> coll() { return Collections.emptyList(); }
            }

            collectionScenarioMixedIn(Customer.class, Customer_orders.class,
                    (processMethodContext, mixeeSpec, facetedMethod, mixedInColl)->{

                // when
                processDomainEvent(facetFactory, processMethodContext);
                postProcessor.postProcessCollection(mixeeSpec, mixedInColl);

                // then - the mixed-in annotation should win
                assertHasCollectionDomainEventFacet(facetedMethod,
                        EventTypeOrigin.ANNOTATED_MEMBER, Customer.OrdersShowingDomainEvent2.class);
            });
        }

    }

    static class TypeOf extends CollectionAnnotationFacetFactoryTest {

        @Test
        void whenCollectionAnnotation() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                @Collection(typeOf = Order.class)
                public List<Order> getOrders() { return null; }
                public void setOrders(final List<Order> orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);
                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacetForCollectionAnnotation);
                assertThat(facet.value().elementType(), CausewayMatchers.classEqualTo(Order.class));
            });
        }

        @Test
        void whenInferFromType() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                public Order[] getOrders() { return null; }
                public void setOrders(final Order[] orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);

                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacet);
                assertThat(facet.value().elementType(), CausewayMatchers.classEqualTo(Order.class));
                assertThat(facet.value().collectionSemantics(), Matchers.is(Optional.of(CollectionSemantics.ARRAY)));
            });
        }

        @Test
        void whenInferFromGenerics() {

            class Order {
            }
            @SuppressWarnings("unused")
            class Customer {
                public java.util.Collection<Order> getOrders() { return null; }
                public void setOrders(final java.util.Collection<Order> orders) {}
            }

            // given
            collectionScenario(Customer.class, "orders", (processMethodContext, facetHolder, facetedMethod)->{
                // when
                processTypeOf(facetFactory, processMethodContext);

                // then
                final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
                assertNotNull(facet);
                assertTrue(facet instanceof TypeOfFacetFromFeature);
                assertThat(facet.value().elementType(), CausewayMatchers.classEqualTo(Order.class));
            });
        }

    }

}
