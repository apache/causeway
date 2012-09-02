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

package org.apache.isis.core.progmodel.facets.collections;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.collections.accessor.CollectionAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.accessor.CollectionAccessorFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.clear.CollectionClearFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.collections.clear.CollectionClearFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddRemoveAndValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddToFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddToFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionRemoveFromFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionRemoveFromFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.modify.TypeOfFacetInferredFromSupportingMethods;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaDescriptionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disable.DisableForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disable.forsession.DisableForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disable.forsession.DisabledFacetViaDisableForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.staticmethod.DisabledFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.members.disable.staticmethod.DisabledFacetViaProtectMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.HiddenFacetAbstract;
import org.apache.isis.core.progmodel.facets.members.hide.HideForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.hide.forsession.HiddenFacetViaHideForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.forsession.HideForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.hide.staticmethod.HiddenFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.members.hide.staticmethod.HiddenFacetViaAlwaysHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaNameMethodFacetFactory;

public class CollectionFieldMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testPropertyAccessorFacetIsInstalledForJavaUtilCollectionAndMethodRemoved() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Collection getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForJavaUtilListAndMethodRemoved() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public List getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForJavaUtilSetAndMethodRemoved() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings({ "rawtypes", "unused" })
            public Set getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForObjectArrayAndMethodRemoved() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public Object[] getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testPropertyAccessorFacetIsInstalledForOrderArrayAndMethodRemoved() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(collectionAccessorMethod));
    }

    public void testAddToFacetIsInstalledViaAccessorIfNoExplicitAddToMethodExists() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAddToFacetViaAccessor);
        final CollectionAddToFacetViaAccessor collectionAddToFacetViaAccessor = (CollectionAddToFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionAddToFacetViaAccessor.getMethods().get(0));
    }

    public void testCannotInferTypeOfFacetIfNoExplicitAddToOrRemoveFromMethods() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TypeOfFacet.class));
    }

    public void testRemoveFromFacetIsInstalledViaAccessorIfNoExplicitRemoveFromMethodExists() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionRemoveFromFacetViaAccessor);
        final CollectionRemoveFromFacetViaAccessor collectionRemoveFromFacetViaAccessor = (CollectionRemoveFromFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionRemoveFromFacetViaAccessor.getMethods().get(0));
    }

    public void testAddToFacetIsInstalledAndMethodRemoved() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAddToFacetViaMethod);
        final CollectionAddToFacetViaMethod collectionAddToFacetViaMethod = (CollectionAddToFacetViaMethod) facet;
        assertEquals(addToMethod, collectionAddToFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(addToMethod));
    }

    public void testCanInferTypeOfFacetFromExplicitAddToMethod() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromSupportingMethods);
        final TypeOfFacetInferredFromSupportingMethods typeOfFacetInferredFromSupportingMethods = (TypeOfFacetInferredFromSupportingMethods) facet;
        assertEquals(Order.class, typeOfFacetInferredFromSupportingMethods.value());
    }

    public void testRemoveFromFacetIsInstalledAndMethodRemoved() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionRemoveFromFacetViaMethod);
        final CollectionRemoveFromFacetViaMethod collectionRemoveFromFacetViaMethod = (CollectionRemoveFromFacetViaMethod) facet;
        assertEquals(removeFromMethod, collectionRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(removeFromMethod));
    }

    public void testCanInferTypeOfFacetFromExplicitRemoveFromMethod() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromSupportingMethods);
        final TypeOfFacetInferredFromSupportingMethods typeOfFacetInferredFromSupportingMethods = (TypeOfFacetInferredFromSupportingMethods) facet;
        assertEquals(Order.class, typeOfFacetInferredFromSupportingMethods.value());
    }

    public void testClearFacetIsInstalledAndMethodRemoved() {
        final CollectionClearFacetFactory facetFactory = new CollectionClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionClearFacetViaMethod);
        final CollectionClearFacetViaMethod collectionClearFacetViaMethod = (CollectionClearFacetViaMethod) facet;
        assertEquals(clearMethod, collectionClearFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(clearMethod));
    }

    public void testClearFacetIsInstalledViaAccessorIfNoExplicitClearMethod() {
        final CollectionClearFacetFactory facetFactory = new CollectionClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionClearFacetViaAccessor);
        final CollectionClearFacetViaAccessor collectionClearFacetViaAccessor = (CollectionClearFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionClearFacetViaAccessor.getMethods().get(0));
    }

    public void testValidateAddToFacetIsInstalledAndMethodRemoved() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionValidateAddToFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionValidateAddToFacetViaMethod);
        final CollectionValidateAddToFacetViaMethod collectionValidateAddToFacetViaMethod = (CollectionValidateAddToFacetViaMethod) facet;
        assertEquals(validateAddToMethod, collectionValidateAddToFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateAddToMethod));
    }

    public void testValidateRemoveFromFacetIsInstalledAndMethodRemoved() {
        final CollectionAddRemoveAndValidateFacetFactory facetFactory = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(Customer.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CollectionValidateRemoveFromFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionValidateRemoveFromFacetViaMethod);
        final CollectionValidateRemoveFromFacetViaMethod collectionValidateRemoveFromFacetViaMethod = (CollectionValidateRemoveFromFacetViaMethod) facet;
        assertEquals(validateRemoveFromMethod, collectionValidateRemoveFromFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateRemoveFromMethod));
    }

    public void testMethodFoundInSuperclass() {
        final CollectionAccessorFacetFactory facetFactory = new CollectionAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

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

        facetFactory.process(new ProcessMethodContext(CustomerEx.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CollectionAccessorFacetViaAccessor);
        final CollectionAccessorFacetViaAccessor collectionAccessorFacetViaMethod = (CollectionAccessorFacetViaAccessor) facet;
        assertEquals(collectionAccessorMethod, collectionAccessorFacetViaMethod.getMethods().get(0));
    }

    public void testMethodFoundInSuperclassButHelpeMethodsFoundInSubclasses() {
        final CollectionAccessorFacetFactory facetFactoryForAccessor = new CollectionAccessorFacetFactory();
        facetFactoryForAccessor.setSpecificationLookup(reflector);
        final CollectionAddRemoveAndValidateFacetFactory facetFactoryForHelpers = new CollectionAddRemoveAndValidateFacetFactory();
        facetFactoryForHelpers.setSpecificationLookup(reflector);

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

        facetFactoryForAccessor.process(new ProcessMethodContext(CustomerEx.class, collectionAccessorMethod, methodRemover, facetedMethod));
        facetFactoryForHelpers.process(new ProcessMethodContext(CustomerEx.class, collectionAccessorMethod, methodRemover, facetedMethod));

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

    public static class CustomerStatic {
        public Collection<Order> getOrders() {
            return null;
        }

        public static String nameOrders() {
            return "Most Recent Orders";
        };

        public static String descriptionOrders() {
            return "Some old description";
        }

        public static boolean alwaysHideOrders() {
            return true;
        }

        public static boolean protectOrders() {
            return true;
        }

        public static boolean hideOrders(final UserMemento userMemento) {
            return true;
        }

        public static String disableOrders(final UserMemento userMemento) {
            return "disabled for this user";
        }

        public static void getOtherOrders() {
        }

        public static boolean alwaysHideOtherOrders() {
            return false;
        }

        public static boolean protectOtherOrders() {
            return false;
        }
    }

    public void testInstallsNamedFacetUsingNameMethodAndRemovesMethod() {
        final NamedFacetViaNameMethodFacetFactory facetFactory = new NamedFacetViaNameMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method nameMethod = findMethod(CustomerStatic.class, "nameOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetViaMethod);
        final NamedFacetViaMethod namedFacet = (NamedFacetViaMethod) facet;
        assertEquals("Most Recent Orders", namedFacet.value());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(nameMethod));
    }

    public void testInstallsDescribedAsFacetUsingDescriptionAndRemovesMethod() {
        final DescribedAsFacetViaDescriptionMethodFacetFactory facetFactory = new DescribedAsFacetViaDescriptionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method descriptionMethod = findMethod(CustomerStatic.class, "descriptionOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DescribedAsFacetViaMethod);
        final DescribedAsFacetViaMethod describedAsFacet = (DescribedAsFacetViaMethod) facet;
        assertEquals("Some old description", describedAsFacet.value());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(descriptionMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideAndRemovesMethod() {
        final HiddenFacetViaAlwaysHideMethodFacetFactory facetFactory = new HiddenFacetViaAlwaysHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAlwaysEverywhere);
        final HiddenFacetAbstract hiddenFacetAlways = (HiddenFacetAlwaysEverywhere) facet;
        assertEquals(When.ALWAYS, hiddenFacetAlways.when());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideWhenNotAndRemovesMethod() {
        final HiddenFacetViaAlwaysHideMethodFacetFactory facetFactory = new HiddenFacetViaAlwaysHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOtherOrders");
        final Method alwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideOtherOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(HiddenFacet.class));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(alwaysHideMethod));
    }

    public void testInstallsDisabledFacetUsingProtectAndRemovesMethod() {
        final DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method protectMethod = findMethod(CustomerStatic.class, "protectOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAlwaysEverywhere);
        final DisabledFacetAlwaysEverywhere disabledFacetAlways = (DisabledFacetAlwaysEverywhere) facet;
        assertEquals(When.ALWAYS, disabledFacetAlways.when());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(protectMethod));
    }

    public void testDoesNotInstallDisabledFacetUsingProtectWhenNotAndRemovesMethod() {
        final DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOtherOrders");
        final Method protectMethod = findMethod(CustomerStatic.class, "protectOtherOrders");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(DisabledFacet.class));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(protectMethod));
    }

    public void testInstallsHiddenForSessionFacetAndRemovesMethod() {
        final HiddenFacetViaHideForSessionMethodFacetFactory facetFactory = new HiddenFacetViaHideForSessionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method hideMethod = findMethod(CustomerStatic.class, "hideOrders", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HideForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForSessionFacetViaMethod);
        final HideForSessionFacetViaMethod hideForSessionFacetViaMethod = (HideForSessionFacetViaMethod) facet;
        assertEquals(hideMethod, hideForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(hideMethod));
    }

    public void testInstallsDisabledForSessionFacetAndRemovesMethod() {
        final DisabledFacetViaDisableForSessionMethodFacetFactory facetFactory = new DisabledFacetViaDisableForSessionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method collectionAccessorMethod = findMethod(CustomerStatic.class, "getOrders");
        final Method disableMethod = findMethod(CustomerStatic.class, "disableOrders", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, collectionAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForSessionFacetViaMethod);
        final DisableForSessionFacetViaMethod disableForSessionFacetViaMethod = (DisableForSessionFacetViaMethod) facet;
        assertEquals(disableMethod, disableForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(disableMethod));
    }

}
