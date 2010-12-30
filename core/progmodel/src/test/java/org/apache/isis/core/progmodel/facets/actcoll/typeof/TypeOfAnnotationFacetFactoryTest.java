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


package org.apache.isis.core.progmodel.facets.actcoll.typeof;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class TypeOfAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private TypeOfAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new TypeOfAnnotationFacetFactory();
        facetFactory.setCollectionTypeRegistry(new CollectionTypeRegistryDefault());
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertTrue(contains(featureTypes, FeatureType.COLLECTION));
        assertTrue(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testTypeOfFacetInferredForActionWithJavaUtilCollectionReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public Collection someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForCollectionWithJavaUtilCollectionReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public Collection getOrders() {
                return null;
            }
        }
        final Method accessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, accessorMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForActionWithGenericCollectionReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            public Collection<Order> someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
        final TypeOfFacetInferredFromGenerics typeOfFacetInferredFromGenerics = (TypeOfFacetInferredFromGenerics) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value());

    }

    public void testTypeOfFacetInferredForCollectionWithGenericCollectionReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            public Collection<Order> getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, collectionAccessorMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
        final TypeOfFacetInferredFromGenerics typeOfFacetInferredFromGenerics = (TypeOfFacetInferredFromGenerics) facet;
        assertEquals(Order.class, typeOfFacetInferredFromGenerics.value());

    }

    public void testTypeOfFacetInferredForActionWithJavaUtilListReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public List someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForCollectionWithJavaUtilListReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public List getOrders() {
                return null;
            }
        }
        final Method accessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, accessorMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForActionWithJavaUtilSetReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public Set someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForCollectionWithJavaUtilSetReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public Set getOrders() {
                return null;
            }
        }
        final Method accessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, accessorMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetViaAnnotation);
        final TypeOfFacetViaAnnotation typeOfFacetViaAnnotation = (TypeOfFacetViaAnnotation) facet;
        assertEquals(Order.class, typeOfFacetViaAnnotation.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetInferredForActionWithArrayReturnType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            public Order[] someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromArray);
        final TypeOfFacetInferredFromArray typeOfFacetInferredFromArray = (TypeOfFacetInferredFromArray) facet;
        assertEquals(Order.class, typeOfFacetInferredFromArray.value());

        assertNoMethodsRemoved();
    }

    public void testTypeOfFacetIsInferredForCollectionFromOrderArray() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            public Order[] getOrders() {
                return null;
            }
        }
        final Method collectionAccessorMethod = findMethod(Customer.class, "getOrders");

        facetFactory.process(Customer.class, collectionAccessorMethod, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(TypeOfFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TypeOfFacetInferredFromArray);
        final TypeOfFacetInferredFromArray typeOfFacetInferredFromArray = (TypeOfFacetInferredFromArray) facet;
        assertEquals(Order.class, typeOfFacetInferredFromArray.value());

    }

    public void testTypeOfAnnotationIgnoredForActionIfReturnTypeIsntACollectionType() {
        class Order {}
        class Customer {
            @SuppressWarnings("unused")
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            @TypeOf(Order.class)
            public Customer someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(Customer.class, actionMethod, methodRemover, facetHolder);

        assertNull(facetHolder.getFacet(TypeOfFacet.class));

        assertNoMethodsRemoved();
    }

}

