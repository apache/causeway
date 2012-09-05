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

package org.apache.isis.core.progmodel.facets.properties;

import java.lang.reflect.Method;

import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyInitializationFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaDescriptionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disabled.DisableForContextFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.DisableForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.forsession.DisableForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disabled.forsession.DisabledFacetViaDisableForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disabled.method.DisabledFacetViaDisableMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.staticmethod.DisabledFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.members.disabled.staticmethod.DisabledFacetViaProtectMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.HideForContextFacet;
import org.apache.isis.core.progmodel.facets.members.hidden.HideForSessionFacet;
import org.apache.isis.core.progmodel.facets.members.hidden.forsession.HiddenFacetViaHideForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.forsession.HideForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.hidden.method.HiddenFacetViaHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.method.HideForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.hidden.staticmethod.HiddenFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.members.hidden.staticmethod.HiddenFacetViaAlwaysHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaNameMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.accessor.PropertyAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.properties.choices.method.PropertyChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.choices.method.PropertyChoicesFacetViaMethod;
import org.apache.isis.core.progmodel.facets.properties.defaults.method.PropertyDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethod;
import org.apache.isis.core.progmodel.facets.properties.derived.inferred.NotPersistableFacetInferred;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyClearFacetViaClearMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyClearFacetViaSetterMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyInitializationFacetViaSetterMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyModifyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetAndClearFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetterFacetViaModifyMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetterFacetViaSetterMethod;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacet;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacetViaMethod;

public class PropertyMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    public void testPropertyAccessorFacetIsInstalledAndMethodRemoved() {
        final PropertyAccessorFacetFactory facetFactory = new PropertyAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
        final PropertyAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (PropertyAccessorFacetViaAccessor) facet;
        assertEquals(propertyAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyAccessorMethod));
    }

    public void testSetterFacetIsInstalledForSetterMethodAndMethodRemoved() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void setFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertySetterMethod = findMethod(Customer.class, "setFirstName", new Class[] { String.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertySetterFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertySetterFacetViaSetterMethod);
        final PropertySetterFacetViaSetterMethod propertySetterFacet = (PropertySetterFacetViaSetterMethod) facet;
        assertEquals(propertySetterMethod, propertySetterFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertySetterMethod));
    }

    public void testInitializationFacetIsInstalledForSetterMethodAndMethodRemoved() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void setFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertySetterMethod = findMethod(Customer.class, "setFirstName", new Class[] { String.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyInitializationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyInitializationFacet);
        final PropertyInitializationFacetViaSetterMethod propertySetterFacet = (PropertyInitializationFacetViaSetterMethod) facet;
        assertEquals(propertySetterMethod, propertySetterFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertySetterMethod));
    }

    public void testSetterFacetIsInstalledMeansNoDisabledOrDerivedFacetsInstalled() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void setFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(NotPersistedFacet.class));
        assertNull(facetedMethod.getFacet(NotPersistedFacet.class));
    }

    public void testSetterFacetIsInstalledForModifyMethodAndMethodRemoved() {

        final PropertyModifyFacetFactory facetFactoryForModify = new PropertyModifyFacetFactory();
        facetFactoryForModify.setSpecificationLookup(reflector);
        final PropertySetAndClearFacetFactory facetFactoryForSetter = new PropertySetAndClearFacetFactory();
        facetFactoryForSetter.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void modifyFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyModifyMethod = findMethod(Customer.class, "modifyFirstName", new Class[] { String.class });

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod);
        facetFactoryForModify.process(processMethodContext);
        facetFactoryForSetter.process(processMethodContext);

        final Facet facet = facetedMethod.getFacet(PropertySetterFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertySetterFacetViaModifyMethod);
        final PropertySetterFacetViaModifyMethod propertySetterFacet = (PropertySetterFacetViaModifyMethod) facet;
        assertEquals(propertyModifyMethod, propertySetterFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyModifyMethod));
    }

    public void testModifyMethodWithNoSetterStillInstallsDisabledAndDerivedFacets() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        final PropertyModifyFacetFactory facetFactoryForModify = new PropertyModifyFacetFactory();
        facetFactoryForModify.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void modifyFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod);
        facetFactory.process(processMethodContext);
        facetFactoryForModify.process(processMethodContext);

        Facet facet = facetedMethod.getFacet(NotPersistedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NotPersistableFacetInferred);

        facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAlwaysEverywhere);
    }

    public void testIfHaveSetterAndModifyFacetThenTheModifyFacetWinsOut() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        final PropertyModifyFacetFactory facetFactoryForModify = new PropertyModifyFacetFactory();
        facetFactoryForModify.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void setFirstName(final String firstName) {
            }

            @SuppressWarnings("unused")
            public void modifyFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertySetterMethod = findMethod(Customer.class, "setFirstName", new Class[] { String.class });
        final Method propertyModifyMethod = findMethod(Customer.class, "modifyFirstName", new Class[] { String.class });

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod);
        facetFactory.process(processMethodContext);
        facetFactoryForModify.process(processMethodContext);

        final Facet facet = facetedMethod.getFacet(PropertySetterFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertySetterFacetViaModifyMethod);
        final PropertySetterFacetViaModifyMethod propertySetterFacet = (PropertySetterFacetViaModifyMethod) facet;
        assertEquals(propertyModifyMethod, propertySetterFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertySetterMethod));
        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyModifyMethod));
    }

    public void testClearFacet() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void clearFirstName() {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyClearMethod = findMethod(Customer.class, "clearFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyClearFacetViaClearMethod);
        final PropertyClearFacetViaClearMethod propertyClearFacet = (PropertyClearFacetViaClearMethod) facet;
        assertEquals(propertyClearMethod, propertyClearFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyClearMethod));
    }

    public void testClearFacetViaSetterIfNoExplicitClearMethod() {
        final PropertySetAndClearFacetFactory facetFactory = new PropertySetAndClearFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public void setFirstName(final String firstName) {
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertySetterMethod = findMethod(Customer.class, "setFirstName", new Class[] { String.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyClearFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyClearFacetViaSetterMethod);
        final PropertyClearFacetViaSetterMethod propertyClearFacet = (PropertyClearFacetViaSetterMethod) facet;
        assertEquals(propertySetterMethod, propertyClearFacet.getMethods().get(0));
    }

    public void testChoicesFacetFoundAndMethodRemoved() {
        final PropertyChoicesFacetFactory facetFactory = new PropertyChoicesFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public Object[] choicesFirstName() {
                return null;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyChoicesMethod = findMethod(Customer.class, "choicesFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyChoicesFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyChoicesFacetViaMethod);
        final PropertyChoicesFacetViaMethod propertyChoicesFacet = (PropertyChoicesFacetViaMethod) facet;
        assertEquals(propertyChoicesMethod, propertyChoicesFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyChoicesMethod));
    }

    public void testDefaultFacetFoundAndMethodRemoved() {
        final PropertyDefaultFacetFactory facetFactory = new PropertyDefaultFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public String defaultFirstName() {
                return null;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyDefaultMethod = findMethod(Customer.class, "defaultFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyDefaultFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyDefaultFacetViaMethod);
        final PropertyDefaultFacetViaMethod propertyDefaultFacet = (PropertyDefaultFacetViaMethod) facet;
        assertEquals(propertyDefaultMethod, propertyDefaultFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDefaultMethod));
    }

    public void testValidateFacetFoundAndMethodRemoved() {
        final PropertyValidateFacetFactory facetFactory = new PropertyValidateFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public String validateFirstName(final String firstName) {
                return null;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyValidateMethod = findMethod(Customer.class, "validateFirstName", new Class[] { String.class });

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyValidateFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyValidateFacetViaMethod);
        final PropertyValidateFacetViaMethod propertyValidateFacet = (PropertyValidateFacetViaMethod) facet;
        assertEquals(propertyValidateMethod, propertyValidateFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyValidateMethod));
    }

    public void testDisableFacetFoundAndMethodRemoved() {
        final DisabledFacetViaDisableMethodFacetFactory facetFactory = new DisabledFacetViaDisableMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public String disableFirstName() {
                return "disabled";
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyDisableMethod = findMethod(Customer.class, "disableFirstName", new Class[] {});

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet;
        assertEquals(propertyDisableMethod, disableForContextFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDisableMethod));
    }

    public void testDisableFacetNoArgsFoundAndMethodRemoved() {
        final DisabledFacetViaDisableMethodFacetFactory facetFactory = new DisabledFacetViaDisableMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public String disableFirstName() {
                return "disabled";
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyDisableMethod = findMethod(Customer.class, "disableFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet;
        assertEquals(propertyDisableMethod, disableForContextFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDisableMethod));
    }

    public void testHiddenFacetFoundAndMethodRemoved() {
        final HiddenFacetViaHideMethodFacetFactory facetFactory = new HiddenFacetViaHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public boolean hideFirstName() {
                return true;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyHideMethod = findMethod(Customer.class, "hideFirstName", new Class[] {});

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForContextFacetViaMethod);
        final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
        assertEquals(propertyHideMethod, hideForContextFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyHideMethod));
    }

    public void testHiddenFacetWithNoArgFoundAndMethodRemoved() {
        final HiddenFacetViaHideMethodFacetFactory facetFactory = new HiddenFacetViaHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }

            @SuppressWarnings("unused")
            public boolean hideFirstName() {
                return true;
            }
        }
        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyHideMethod = findMethod(Customer.class, "hideFirstName");

        facetFactory.process(new ProcessMethodContext(Customer.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForContextFacetViaMethod);
        final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
        assertEquals(propertyHideMethod, hideForContextFacet.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyHideMethod));
    }

    public void testPropertyFoundOnSuperclass() {
        final PropertyAccessorFacetFactory facetFactory = new PropertyAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }
        }

        class CustomerEx extends Customer {
        }

        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerEx.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
        final PropertyAccessorFacetViaAccessor accessorFacet = (PropertyAccessorFacetViaAccessor) facet;
        assertEquals(propertyAccessorMethod, accessorFacet.getMethods().get(0));
    }

    public void testPropertyFoundOnSuperclassButHelperMethodFoundOnSubclass() {
        final PropertyAccessorFacetFactory facetFactory = new PropertyAccessorFacetFactory();
        facetFactory.setSpecificationLookup(reflector);
        final HiddenFacetViaHideMethodFacetFactory facetFactoryForHide = new HiddenFacetViaHideMethodFacetFactory();
        facetFactoryForHide.setSpecificationLookup(reflector);
        final DisabledFacetViaDisableMethodFacetFactory facetFactoryForDisable = new DisabledFacetViaDisableMethodFacetFactory();
        facetFactoryForDisable.setSpecificationLookup(reflector);

        class Customer {
            @SuppressWarnings("unused")
            public String getFirstName() {
                return null;
            }
        }

        class CustomerEx extends Customer {
            @SuppressWarnings("unused")
            public boolean hideFirstName() {
                return true;
            }

            @SuppressWarnings("unused")
            public String disableFirstName() {
                return "disabled";
            }
        }

        final Method propertyAccessorMethod = findMethod(Customer.class, "getFirstName");
        final Method propertyHideMethod = findMethod(CustomerEx.class, "hideFirstName");
        final Method propertyDisableMethod = findMethod(CustomerEx.class, "disableFirstName");

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(CustomerEx.class, propertyAccessorMethod, methodRemover, facetedMethod);
        facetFactory.process(processMethodContext);
        facetFactoryForHide.process(processMethodContext);
        facetFactoryForDisable.process(processMethodContext);

        final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HideForContextFacetViaMethod);
        final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
        assertEquals(propertyHideMethod, hideForContextFacet.getMethods().get(0));

        final Facet facet2 = facetedMethod.getFacet(DisableForContextFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet2;
        assertEquals(propertyDisableMethod, disableForContextFacet.getMethods().get(0));
    }

    public static class CustomerStatic {
        public String getFirstName() {
            return null;
        }

        // required otherwise marked as DisabledFacetAlways
        public void setFirstName(final String firstName) {
        }

        public static String nameFirstName() {
            return "Given name";
        };

        public static String descriptionFirstName() {
            return "Some old description";
        }

        public static boolean alwaysHideFirstName() {
            return true;
        }

        public static boolean protectFirstName() {
            return true;
        }

        public static boolean hideFirstName(final UserMemento userMemento) {
            return true;
        }

        public static String disableFirstName(final UserMemento userMemento) {
            return "disabled for this user";
        }

        public String getLastName() {
            return null;
        }

        // required otherwise marked as DisabledFacetAlways
        public void setLastName(final String firstName) {
        }

        public static boolean alwaysHideLastName() {
            return false;
        }

        public static boolean protectLastName() {
            return false;
        }
    }

    public void testInstallsNamedFacetUsingNameMethodAndRemovesMethod() {
        final NamedFacetViaNameMethodFacetFactory facetFactory = new NamedFacetViaNameMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method nameMethod = findMethod(CustomerStatic.class, "nameFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NamedFacetViaMethod);
        final NamedFacetViaMethod namedFacet = (NamedFacetViaMethod) facet;
        assertEquals("Given name", namedFacet.value());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(nameMethod));
    }

    public void testInstallsDescribedAsFacetUsingDescriptionAndRemovesMethod() {
        final DescribedAsFacetViaDescriptionMethodFacetFactory facetFactory = new DescribedAsFacetViaDescriptionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method descriptionMethod = findMethod(CustomerStatic.class, "descriptionFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

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

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method propertyAlwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HiddenFacetAlwaysEverywhere);

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyAlwaysHideMethod));
    }

    public void testInstallsHiddenFacetUsingAlwaysHideWhenNotAndRemovesMethod() {
        final HiddenFacetViaAlwaysHideMethodFacetFactory facetFactory = new HiddenFacetViaAlwaysHideMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getLastName");
        final Method propertyAlwaysHideMethod = findMethod(CustomerStatic.class, "alwaysHideLastName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(HiddenFacet.class));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyAlwaysHideMethod));
    }

    public void testInstallsDisabledFacetUsingProtectAndRemovesMethod() {
        final DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method propertyProtectMethod = findMethod(CustomerStatic.class, "protectFirstName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAlwaysEverywhere);

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyProtectMethod));
    }

    public void testDoesNotInstallDisabledFacetUsingProtectWhenNotAndRemovesMethod() {
        final DisabledFacetViaProtectMethodFacetFactory facetFactory = new DisabledFacetViaProtectMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getLastName");
        final Method propertyProtectMethod = findMethod(CustomerStatic.class, "protectLastName");

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNull(facet);

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyProtectMethod));
    }

    public void testInstallsHiddenForSessionFacetAndRemovesMethod() {
        final HiddenFacetViaHideForSessionMethodFacetFactory facetFactory = new HiddenFacetViaHideForSessionMethodFacetFactory();
        facetFactory.setSpecificationLookup(reflector);

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method hideMethod = findMethod(CustomerStatic.class, "hideFirstName", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

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

        final Method propertyAccessorMethod = findMethod(CustomerStatic.class, "getFirstName");
        final Method disableMethod = findMethod(CustomerStatic.class, "disableFirstName", new Class[] { UserMemento.class });

        facetFactory.process(new ProcessMethodContext(CustomerStatic.class, propertyAccessorMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisableForSessionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisableForSessionFacetViaMethod);
        final DisableForSessionFacetViaMethod disableForSessionFacetViaMethod = (DisableForSessionFacetViaMethod) facet;
        assertEquals(disableMethod, disableForSessionFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(disableMethod));
    }

}
