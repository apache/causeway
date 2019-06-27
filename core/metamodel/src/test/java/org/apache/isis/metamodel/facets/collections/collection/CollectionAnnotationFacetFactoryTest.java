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
package org.apache.isis.metamodel.facets.collections.collection;

import java.lang.reflect.Method;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.collections.collection.disabled.DisabledFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetDefault;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacetAbstract;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacetAbstract;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CollectionAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    CollectionAnnotationFacetFactory facetFactory;
    Method collectionMethod;

    @Mock
    ObjectSpecification mockTypeSpec;
    @Mock
    ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {
            {
                oneOf(mockMethodRemover).removeMethod(actionMethod);
            }
        });
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLoader).loadSpecification(cls);
                will(returnValue(mockTypeSpec));

                allowing(mockSpecificationLoader).loadSpecification(returnType);
                will(returnValue(mockReturnTypeSpec));
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        facetFactory = new CollectionAnnotationFacetFactory();
    }

    @Override
	@After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class Modify extends CollectionAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(mockOnType, holder) {
                @Override
                public Object getProperty(
                        final ManagedObject inObject,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return null;
                }
            });
        }

        private void addAddToFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new CollectionAddToFacetAbstract(holder) {
                @Override
                public void add(
                        final ObjectAdapter inObject,
                        final ObjectAdapter value,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                }
            });
        }

        private void addRemoveFromFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new CollectionRemoveFromFacetAbstract(holder) {
                @Override
                public void remove(
                        final ObjectAdapter inObject,
                        final ObjectAdapter element,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                }
            });
        }

        // @Test
        public void withDeprecatedPostsCollectionAddedToEvent_andGetterFacet_andSetterFacet() {

            class Order {
            }
            class Customer {
                class OrdersAddedToOrRemovedFromDomainEvent extends CollectionDomainEvent<Customer, Order> {
                }

                @Collection(domainEvent = OrdersAddedToOrRemovedFromDomainEvent.class)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            addGetterFacet(facetedMethod);
            addAddToFacet(facetedMethod);
            addRemoveFromFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, collectionMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetDefault);
            final CollectionDomainEventFacetDefault domainEventFacetDefault = (CollectionDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetDefault.getEventType(), classEqualTo(CollectionDomainEvent.Default.class)); // this
                                                                                                                   // is
                                                                                                                   // discarded
                                                                                                                   // at
                                                                                                                   // runtime,
                                                                                                                   // see
                                                                                                                   // PropertySetterFacetForPostsPropertyChangedEventAnnotation#verify(...)

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForDomainEventFromCollectionAnnotation);
            final CollectionAddToFacetForDomainEventFromCollectionAnnotation addToFacetImpl = (CollectionAddToFacetForDomainEventFromCollectionAnnotation) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(Customer.OrdersAddedToOrRemovedFromDomainEvent.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation);
            final CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation removeFromFacetImpl = (CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(Customer.OrdersAddedToOrRemovedFromDomainEvent.class));
        }

        // @Test
        public void withCollectionInteractionEvent() {

            class Order {
            }
            class Customer {
                class OrdersChangedDomainEvent extends CollectionDomainEvent<Customer, Order> {
                }

                @Collection(domainEvent = OrdersChangedDomainEvent.class)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            addGetterFacet(facetedMethod);
            addAddToFacet(facetedMethod);
            addRemoveFromFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, collectionMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetForCollectionAnnotation);
            final CollectionDomainEventFacetForCollectionAnnotation domainEventFacetImpl = (CollectionDomainEventFacetForCollectionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(Customer.OrdersChangedDomainEvent.class));

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForDomainEventFromCollectionAnnotation);
            final CollectionAddToFacetForDomainEventFromCollectionAnnotation addToFacetImpl = (CollectionAddToFacetForDomainEventFromCollectionAnnotation) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(Customer.OrdersChangedDomainEvent.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation);
            final CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation removeFromFacetImpl = (CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(Customer.OrdersChangedDomainEvent.class));
        }

        // @Test
        public void withCollectionDomainEvent() {

            class Order {
            }
            class Customer {
                class OrdersChanged extends CollectionDomainEvent<Customer, Order> {
                }
                @Collection(domainEvent = OrdersChanged.class)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            addGetterFacet(facetedMethod);
            addAddToFacet(facetedMethod);
            addRemoveFromFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, collectionMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetForCollectionAnnotation);
            final CollectionDomainEventFacetForCollectionAnnotation domainEventFacetImpl = (CollectionDomainEventFacetForCollectionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForDomainEventFromCollectionAnnotation);
            final CollectionAddToFacetForDomainEventFromCollectionAnnotation addToFacetImpl = (CollectionAddToFacetForDomainEventFromCollectionAnnotation) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation);
            final CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation removeFromFacetImpl = (CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));
        }

        // @Test
        public void withDefaultEvent() {

            class Order {
            }
            class Customer {
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            addGetterFacet(facetedMethod);
            addAddToFacet(facetedMethod);
            addRemoveFromFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, collectionMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetDefault);
            final CollectionDomainEventFacetDefault domainEventFacetImpl = (CollectionDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(CollectionDomainEvent.Default.class));

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForDomainEventFromDefault);
            final CollectionAddToFacetForDomainEventFromDefault addToFacetImpl = (CollectionAddToFacetForDomainEventFromDefault) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(CollectionDomainEvent.Default.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForDomainEventFromDefault);
            final CollectionRemoveFromFacetForDomainEventFromDefault removeFromFacetImpl = (CollectionRemoveFromFacetForDomainEventFromDefault) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(CollectionDomainEvent.Default.class));
        }
    }

    public static class Hidden extends CollectionAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Order {
            }
            class Customer {
                @Collection(hidden = Where.REFERENCES_PARENT)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processHidden(processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            Assert.assertTrue(hiddenFacet instanceof HiddenFacetForCollectionAnnotation);
            final HiddenFacetForCollectionAnnotation hiddenFacetImpl = (HiddenFacetForCollectionAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));

            final Facet hiddenFacetForColl = facetedMethod.getFacet(HiddenFacetForCollectionAnnotation.class);
            Assert.assertNotNull(hiddenFacetForColl);
            Assert.assertTrue(hiddenFacet == hiddenFacetForColl);
        }

    }

    public static class Editing extends CollectionAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Order {
            }
            class Customer {
                @Collection(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the orders collection"
                        )
                        public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processEditing(processMethodContext);

            // then
            final DisabledFacet disabledFacet = facetedMethod.getFacet(DisabledFacet.class);
            Assert.assertNotNull(disabledFacet);
            Assert.assertTrue(disabledFacet instanceof DisabledFacetForCollectionAnnotation);
            final DisabledFacetForCollectionAnnotation disabledFacetImpl = (DisabledFacetForCollectionAnnotation) disabledFacet;
            assertThat(disabledFacet.where(), is(Where.EVERYWHERE));
            assertThat(disabledFacetImpl.getReason(), is("you cannot edit the orders collection"));
        }
    }

    public static class TypeOf extends CollectionAnnotationFacetFactoryTest {


        @Test
        public void whenCollectionAnnotation() {

            class Order {
            }
            class Customer {
                @Collection(typeOf = Order.class)
                public List<Order> getOrders() {
                    return null;
                }

                public void setOrders(final List<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetOnCollectionFromCollectionAnnotation);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromType() {

            class Order {
            }
            class Customer {
                public Order[] getOrders() {
                    return null;
                }

                public void setOrders(final Order[] orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromArray);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromGenerics() {

            class Order {
            }
            class Customer {
                public java.util.Collection<Order> getOrders() {
                    return null;
                }

                public void setOrders(final java.util.Collection<Order> orders) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            collectionMethod = findMethod(Customer.class, "getOrders");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls,
                    null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

    }

}