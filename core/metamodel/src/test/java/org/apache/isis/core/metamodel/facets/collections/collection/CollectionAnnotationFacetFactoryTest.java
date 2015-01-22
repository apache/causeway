package org.apache.isis.core.metamodel.facets.collections.collection;

import java.lang.reflect.Method;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionInteraction;
import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.annotation.PostsCollectionRemovedFromEvent;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionRemovedFromEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.disabled.DisabledFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForPostsCollectionAddedToEventAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromTypeOfAnnotation;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

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
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoaderSpi).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));
        }});
    }

    @Before
    public void setUp() throws Exception {
        facetFactory = new CollectionAnnotationFacetFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class Modify extends CollectionAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(holder) {
                @Override
                public Object getProperty(final ObjectAdapter inObject) {
                    return null;
                }
            });
        }

        private void addAddToFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new CollectionAddToFacetAbstract(holder) {
                @Override
                public void add(final ObjectAdapter inObject, final ObjectAdapter value) {
                }
            });
        }

        private void addRemoveFromFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new CollectionRemoveFromFacetAbstract(holder) {
                @Override
                public void remove(final ObjectAdapter inObject, final ObjectAdapter element) {
                }
            });
        }


        @Test
        public void withDeprecatedPostsCollectionAddedToEvent_andGetterFacet_andSetterFacet() {

            class Order{}
            class Customer {
                class OrdersAddedTo extends CollectionAddedToEvent<Customer, Order> {
                    public OrdersAddedTo(final Customer source, final Identifier identifier, final Order value) {
                        super(source, identifier, value);
                    }
                }
                class OrdersRemovedFrom extends CollectionRemovedFromEvent<Customer, Order> {
                    public OrdersRemovedFrom(final Customer source, final Identifier identifier, final Order value) {
                        super(source, identifier, value);
                    }
                }
                @PostsCollectionAddedToEvent(OrdersAddedTo.class)
                @PostsCollectionRemovedFromEvent(OrdersRemovedFrom.class)
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetDefault);
            final CollectionDomainEventFacetDefault domainEventFacetDefault = (CollectionDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetDefault.getEventType(), classEqualTo(CollectionDomainEvent.Default.class)); // this is discarded at runtime, see PropertySetterFacetForPostsPropertyChangedEventAnnotation#verify(...)

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForPostsCollectionAddedToEventAnnotation);
            final CollectionAddToFacetForPostsCollectionAddedToEventAnnotation addToFacetImpl = (CollectionAddToFacetForPostsCollectionAddedToEventAnnotation) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(Customer.OrdersAddedTo.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation);
            final CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation removeFromFacetImpl = (CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(Customer.OrdersRemovedFrom.class));
        }


        @Test
        public void withCollectionInteractionEvent() {

            class Order{}
            class Customer {
                class OrdersChanged extends CollectionInteractionEvent<Customer, Order> {
                    public OrdersChanged(final Customer source, final Identifier identifier, final Of of) {
                        super(source, identifier, of);
                    }

                    public OrdersChanged(final Customer source, final Identifier identifier, final Of of, final Order value) {
                        super(source, identifier, of, value);
                    }
                }
                @CollectionInteraction(OrdersChanged.class)
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(CollectionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof CollectionDomainEventFacetForCollectionInteractionAnnotation);
            final CollectionDomainEventFacetForCollectionInteractionAnnotation domainEventFacetImpl = (CollectionDomainEventFacetForCollectionInteractionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));

            // then
            final Facet addToFacet = facetedMethod.getFacet(CollectionAddToFacet.class);
            Assert.assertNotNull(addToFacet);
            Assert.assertTrue(addToFacet instanceof CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation);
            final CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation addToFacetImpl = (CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation) addToFacet;
            assertThat(addToFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));

            // then
            final Facet removeFromFacet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
            Assert.assertNotNull(removeFromFacet);
            Assert.assertTrue(removeFromFacet instanceof CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation);
            final CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation removeFromFacetImpl = (CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation) removeFromFacet;
            assertThat(removeFromFacetImpl.value(), classEqualTo(Customer.OrdersChanged.class));
        }

        @Test
        public void withCollectionDomainEvent() {

            class Order{}
            class Customer {
                class OrdersChanged extends CollectionDomainEvent<Customer, Order> {
                    public OrdersChanged(final Customer source, final Identifier identifier, final Of of) {
                        super(source, identifier, of);
                    }

                    public OrdersChanged(final Customer source, final Identifier identifier, final Of of, final Order value) {
                        super(source, identifier, of, value);
                    }
                }
                @Collection(domainEvent=OrdersChanged.class)
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
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

        @Test
        public void withDefaultEvent() {

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
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

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processHidden(processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            Assert.assertTrue(hiddenFacet instanceof HiddenFacetForCollectionAnnotation);
            final HiddenFacetForCollectionAnnotation hiddenFacetImpl = (HiddenFacetForCollectionAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));
            assertThat(hiddenFacetImpl.when(), is(When.ALWAYS));

            final Facet hiddenFacetForColl = facetedMethod.getFacet(HiddenFacetForCollectionAnnotation.class);
            Assert.assertNotNull(hiddenFacetForColl);
            Assert.assertTrue(hiddenFacet == hiddenFacetForColl);
        }

    }

    public static class Editing extends CollectionAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processEditing(processMethodContext);

            // then
            final DisabledFacet disabledFacet = facetedMethod.getFacet(DisabledFacet.class);
            Assert.assertNotNull(disabledFacet);
            Assert.assertTrue(disabledFacet instanceof DisabledFacetForCollectionAnnotation);
            final DisabledFacetForCollectionAnnotation disabledFacetImpl = (DisabledFacetForCollectionAnnotation) disabledFacet;
            assertThat(disabledFacet.where(), is(Where.EVERYWHERE));
            assertThat(disabledFacet.when(), is(When.ALWAYS));
            assertThat(disabledFacetImpl.getReason(), is("you cannot edit the orders collection"));
        }
    }

    public static class TypeOf extends CollectionAnnotationFacetFactoryTest {

        @Test
        public void whenDeprecatedTypeOfAnnotation() {

            class Order{}
            class Customer {
                @org.apache.isis.applib.annotation.TypeOf(Order.class)
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetOnCollectionFromTypeOfAnnotation);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenCollectionAnnotation() {

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetOnCollectionFromCollectionAnnotation);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromType() {

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromArray);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromGenerics() {

            class Order{}
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
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, collectionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

    }

}