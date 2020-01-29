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

package org.apache.isis.core.metamodel.facets.collections;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Rule;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacetViaMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToRemoveFromAndValidateFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacetViaMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.TypeOfFacetInferredFromSupportingMethods;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateAddToFacetViaMethod;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateRemoveFromFacetViaMethod;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.clear.CollectionClearFacetViaAccessor;
import org.apache.isis.core.metamodel.facets.collections.clear.CollectionClearFacetViaClearMethod;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;

import lombok.val;

public class CollectionFieldMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectSpecification mockSpecification;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // expecting
        allowing_specificationLoader_loadSpecification_any_willReturn(mockSpecification);
    }

    public void testPropertyAccessorFacetIsInstalledForJavaUtilCollectionAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }


    public void testPropertyAccessorFacetIsInstalledForJavaUtilListAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);
        
        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public List getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");


        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForJavaUtilSetAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Set getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForObjectArrayAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        class Customer {
            @SuppressWarnings("unused")
            public Object[] getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForOrderArrayAndMethodRemoved() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Order[] getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testAddToFacetIsInstalledViaAccessorIfNoExplicitAddToMethodExists() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAddToFacetViaAccessor);
        final CollectionAddToFacetViaAccessor collectionAddToFacetViaAccessor = (CollectionAddToFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionAddToFacetViaAccessor.getMethods().get(0));
    }

    public void testCannotInferTypeOfFacetIfNoExplicitAddToOrRemoveFromMethods() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TypeOfFacet.class));
    }

    public void testRemoveFromFacetIsInstalledViaAccessorIfNoExplicitRemoveFromMethodExists() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionRemoveFromFacetViaAccessor);
        final CollectionRemoveFromFacetViaAccessor collectionRemoveFromFacetViaAccessor = (CollectionRemoveFromFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionRemoveFromFacetViaAccessor.getMethods().get(0));
    }

    public void testAddToFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method addToMethod = findMethod(Customer.class, "addToOrders", new Class[] { Order.class });

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAddToFacetViaMethod);
        final CollectionAddToFacetViaMethod collectionAddToFacetViaMethod = (CollectionAddToFacetViaMethod) facet;
        assertEquals(addToMethod, collectionAddToFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(addToMethod));
    }

    public void testCanInferTypeOfFacetFromExplicitAddToMethod() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromSupportingMethods);
        final TypeOfFacetInferredFromSupportingMethods typeOfFacetInferredFromSupportingMethods = (TypeOfFacetInferredFromSupportingMethods) facet;
        assertEquals(Order.class, typeOfFacetInferredFromSupportingMethods.value());
    }

    public void testRemoveFromFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void removeFromOrders(final Order o) {
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method removeFromMethod = findMethod(Customer.class, "removeFromOrders", new Class[] { Order.class });

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionRemoveFromFacetViaMethod);
        final CollectionRemoveFromFacetViaMethod collectionRemoveFromFacetViaMethod = (CollectionRemoveFromFacetViaMethod) facet;
        assertEquals(removeFromMethod, collectionRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(removeFromMethod));
    }

    public void testCanInferTypeOfFacetFromExplicitRemoveFromMethod() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            };

            @SuppressWarnings("unused")
            public void removeFromOrders(final Order o) {
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromSupportingMethods);
        final TypeOfFacetInferredFromSupportingMethods typeOfFacetInferredFromSupportingMethods = (TypeOfFacetInferredFromSupportingMethods) facet;
        assertEquals(Order.class, typeOfFacetInferredFromSupportingMethods.value());
    }

    public void testClearFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new CollectionClearFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings({ "hiding", "unused" })
        class Order {
        }
        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void clearOrders() {
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method clearMethod = findMethod(Customer.class, "clearOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionClearFacetViaClearMethod);
        final CollectionClearFacetViaClearMethod collectionClearFacetViaClearMethod = (CollectionClearFacetViaClearMethod) facet;
        assertEquals(clearMethod, collectionClearFacetViaClearMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(clearMethod));
    }

    public void testClearFacetIsInstalledViaAccessorIfNoExplicitClearMethod() {
        val facetFactory = new CollectionClearFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings({ "hiding", "unused" })
        class Order {
        }
        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionClearFacetViaAccessor);
        final CollectionClearFacetViaAccessor collectionClearFacetViaAccessor = (CollectionClearFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionClearFacetViaAccessor.getMethods().get(0));
    }

    public void testValidateAddToFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void addToOrders(final Order o) {
            };

            @SuppressWarnings("unused")
            public String validateAddToOrders(final Order o) {
                return null;
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method validateAddToMethod = findMethod(Customer.class, "validateAddToOrders", new Class[] { Order.class });

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionValidateAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionValidateAddToFacetViaMethod);
        final CollectionValidateAddToFacetViaMethod collectionValidateAddToFacetViaMethod = (CollectionValidateAddToFacetViaMethod) facet;
        assertEquals(validateAddToMethod, collectionValidateAddToFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateAddToMethod));
    }

    public void testValidateRemoveFromFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }

            @SuppressWarnings("unused")
            public void removeFromOrders(final Order o) {
            };

            @SuppressWarnings("unused")
            public String validateRemoveFromOrders(final Order o) {
                return null;
            };
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method validateRemoveFromMethod = findMethod(Customer.class, "validateRemoveFromOrders", new Class[] { Order.class });

        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionValidateRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionValidateRemoveFromFacetViaMethod);
        final CollectionValidateRemoveFromFacetViaMethod collectionValidateRemoveFromFacetViaMethod = (CollectionValidateRemoveFromFacetViaMethod) facet;
        assertEquals(validateRemoveFromMethod, collectionValidateRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateRemoveFromMethod));
    }

    public void testMethodFoundInSuperclass() {
        val facetFactory = new CollectionAccessorFacetViaAccessorFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }

        class CustomerEx extends Customer {
        }

        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerEx.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor collectionAccessorFacetViaMethod = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionAccessorFacetViaMethod.getMethods().get(0));
    }

    public void testMethodFoundInSuperclassButHelpeMethodsFoundInSubclasses() {
        val facetFactoryForAccessor = new CollectionAccessorFacetViaAccessorFactory();
        val facetFactoryForHelpers = new CollectionAddToRemoveFromAndValidateFacetFactory();
        facetFactoryForAccessor.setMetaModelContext(super.metaModelContext);
        facetFactoryForHelpers.setMetaModelContext(super.metaModelContext);

        @SuppressWarnings("hiding")
        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> getOrders() {
                return null;
            }
        }

        class CustomerEx extends Customer {
            @SuppressWarnings("unused")
            public void removeFromOrders(final Order o) {
            };

            @SuppressWarnings("unused")
            public String validateRemoveFromOrders(final Order o) {
                return null;
            };
        }

        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");
        final Method removeFromMethod = findMethod(CustomerEx.class, "removeFromOrders", new Class[] { Order.class });
        final Method validateRemoveFromMethod = findMethod(CustomerEx.class, "validateRemoveFromOrders", new Class[] { Order.class });

        facetFactoryForAccessor.process(new FacetFactory.ProcessMethodContext(CustomerEx.class, null, collectionAccessorMethod, methodRemover, facetedMethod));
        facetFactoryForHelpers.process(new FacetFactory.ProcessMethodContext(CustomerEx.class, null, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionRemoveFromFacetViaMethod);
        final CollectionRemoveFromFacetViaMethod collectionRemoveFromFacetViaMethod = (CollectionRemoveFromFacetViaMethod) facet;
        assertEquals(removeFromMethod, collectionRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(removeFromMethod));

        final Facet facet1 = facetedMethod.getFacet(CollectionValidateRemoveFromFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof CollectionValidateRemoveFromFacetViaMethod);
        final CollectionValidateRemoveFromFacetViaMethod collectionValidateRemoveFromFacetViaMethod = (CollectionValidateRemoveFromFacetViaMethod) facet1;
        assertEquals(validateRemoveFromMethod, collectionValidateRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateRemoveFromMethod));
    }

    static class Order {
    }


}
