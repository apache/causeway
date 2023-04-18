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
package org.apache.causeway.core.metamodel.facets.properties;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.members.hidden.method.HideForContextFacet;
import org.apache.causeway.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.causeway.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.causeway.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessorFactory;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethod;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethodFactory;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.properties.update.PropertySetterFacetFactory;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaSetterMethod;
import org.apache.causeway.core.metamodel.facets.properties.update.init.PropertyInitializationFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.init.PropertyInitializationFacetViaSetterMethod;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaSetterMethod;
import org.apache.causeway.core.metamodel.facets.properties.validating.PropertyValidateFacet;
import org.apache.causeway.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethodFactory;

import lombok.val;

class PropertyMethodsFacetFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void propertyAccessorFacetIsInstalledAndMethodRemoved() {
        val facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        final Method propertyAccessorMethod = findMethodExactOrFail(Customer.class, "getFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
            final PropertyAccessorFacetViaAccessor propertyAccessorFacetViaAccessor = (PropertyAccessorFacetViaAccessor) facet;
            assertMethodEquals(propertyAccessorMethod, propertyAccessorFacetViaAccessor.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyAccessorMethod));
        });
    }

    @Test
    void setterFacetIsInstalledForSetterMethodAndMethodRemoved() {
        val facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) { }
        }
        final Method propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertySetterFacetViaSetterMethod);
            final PropertySetterFacetViaSetterMethod propertySetterFacet = (PropertySetterFacetViaSetterMethod) facet;
            assertMethodEquals(propertySetterMethod, propertySetterFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertySetterMethod));
        });
    }

    @Test
    void initializationFacetIsInstalledForSetterMethodAndMethodRemoved() {
        val facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) {}
        }
        final Method propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyInitializationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyInitializationFacet);
            final PropertyInitializationFacetViaSetterMethod propertySetterFacet = (PropertyInitializationFacetViaSetterMethod) facet;
            assertMethodEquals(propertySetterMethod, propertySetterFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertySetterMethod));
        });
    }

    @Test
    void setterFacetIsInstalledMeansNoDisabledOrDerivedFacetsInstalled() {
        val facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) {}
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            assertNull(facetedMethod.getFacet(SnapshotExcludeFacet.class));
        });
    }

    @Test
    void clearFacetViaSetterIfNoExplicitClearMethod() {
        val facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) { }
        }
        final Method propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyClearFacetViaSetterMethod);
            final PropertyClearFacetViaSetterMethod propertyClearFacet = (PropertyClearFacetViaSetterMethod) facet;
            assertMethodEquals(propertySetterMethod, propertyClearFacet.getMethods().getFirstElseFail().asMethodElseFail());
        });
    }

    @Test
    void choicesFacetFoundAndMethodRemoved() {
        val facetFactory = new PropertyChoicesFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String[] choicesFirstName() { return null; }
        }

        final Method propertyChoicesMethod = findMethodExactOrFail(Customer.class, "choicesFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyChoicesFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyChoicesFacetViaMethod);
            final PropertyChoicesFacetViaMethod propertyChoicesFacet = (PropertyChoicesFacetViaMethod) facet;
            assertMethodEquals(propertyChoicesMethod, propertyChoicesFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyChoicesMethod));
        });
    }

    @Test
    void autoCompleteFacetFoundAndMethodRemoved() {
        val facetFactory = new PropertyAutoCompleteFacetMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String[] autoCompleteFirstName(final String searchArg) { return null; }
        }

        final Method propertyAutoCompleteMethod = findMethodExactOrFail(Customer.class, "autoCompleteFirstName", new Class[]{String.class});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyAutoCompleteFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAutoCompleteFacetMethod);
            final PropertyAutoCompleteFacetMethod propertyAutoCompleteFacet = (PropertyAutoCompleteFacetMethod) facet;
            assertMethodEquals(propertyAutoCompleteMethod, propertyAutoCompleteFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyAutoCompleteMethod));
        });
    }

    @Test
    void defaultFacetFoundAndMethodRemoved() {
        val facetFactory = new PropertyDefaultFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String defaultFirstName() { return null; }
        }

        final Method propertyDefaultMethod = findMethodExactOrFail(Customer.class, "defaultFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyDefaultFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyDefaultFacetViaMethod);
            final PropertyDefaultFacetViaMethod propertyDefaultFacet = (PropertyDefaultFacetViaMethod) facet;
            assertMethodEquals(propertyDefaultMethod, propertyDefaultFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDefaultMethod));
        });
    }

    @Test
    void validateFacetFoundAndMethodRemoved() {
        val facetFactory = new PropertyValidateFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String validateFirstName(final String firstName) { return null;}
        }

        final Method propertyValidateMethod = findMethodExactOrFail(Customer.class, "validateFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyValidateFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyValidateFacetViaMethod);
            final PropertyValidateFacetViaMethod propertyValidateFacet = (PropertyValidateFacetViaMethod) facet;
            assertMethodEquals(propertyValidateMethod, propertyValidateFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyValidateMethod));
        });
    }

    @Test
    void disableFacetFoundAndMethodRemoved() {
        val facetFactory = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String disableFirstName() { return "disabled"; }
        }

        final Method propertyDisableMethod = findMethodExactOrFail(Customer.class, "disableFirstName", new Class[] {});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DisableForContextFacetViaMethod);
            final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet;
            assertMethodEquals(propertyDisableMethod, disableForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDisableMethod));
        });
    }

    @Test
    void disableFacetNoArgsFoundAndMethodRemoved() {
        val facetFactory = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String disableFirstName() { return "disabled"; }
        }

        final Method propertyDisableMethod = findMethodExactOrFail(Customer.class, "disableFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DisableForContextFacetViaMethod);
            final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet;
            assertMethodEquals(propertyDisableMethod, disableForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyDisableMethod));
        });
    }

    @Test
    void hiddenFacetFoundAndMethodRemoved() {
        val facetFactory = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public boolean hideFirstName() { return true; }
        }

        final Method propertyHideMethod = findMethodExactOrFail(Customer.class, "hideFirstName", new Class[] {});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEquals(propertyHideMethod, hideForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyHideMethod));
        });
    }

    @Test
    void hiddenFacetWithNoArgFoundAndMethodRemoved() {
        val facetFactory = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public boolean hideFirstName() { return true; }
        }

        final Method propertyHideMethod = findMethodExactOrFail(Customer.class, "hideFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEquals(propertyHideMethod, hideForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(propertyHideMethod));
        });
    }

    @Test
    void propertyFoundOnSuperclass() {
        val facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        class CustomerEx extends Customer {
        }

        final Method propertyAccessorMethod = findMethodExactOrFail(CustomerEx.class, "getFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
            final PropertyAccessorFacetViaAccessor accessorFacet = (PropertyAccessorFacetViaAccessor) facet;
            assertMethodEquals(propertyAccessorMethod, accessorFacet.getMethods().getFirstElseFail().asMethodElseFail());
        });

    }

    @Test
    void propertyFoundOnSuperclassButHelperMethodFoundOnSubclass() {
        val facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        val facetFactoryForHide = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        val facetFactoryForDisable = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        @SuppressWarnings("unused")
        class CustomerEx extends Customer {
            public boolean hideFirstName() { return true; }
            public String disableFirstName() { return "disabled";}
        }

        final Method propertyHideMethod = findMethodExactOrFail(CustomerEx.class, "hideFirstName");
        final Method propertyDisableMethod = findMethodExactOrFail(CustomerEx.class, "disableFirstName");

        propertyScenario(CustomerEx.class, "firstName", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            facetFactory.process(processMethodContext);
            facetFactoryForHide.process(processMethodContext);
            facetFactoryForDisable.process(processMethodContext);

            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            final HideForContextFacetViaMethod hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEquals(propertyHideMethod, hideForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());

            final Facet facet2 = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet2);
            assertTrue(facet2 instanceof DisableForContextFacetViaMethod);
            final DisableForContextFacetViaMethod disableForContextFacet = (DisableForContextFacetViaMethod) facet2;
            assertMethodEquals(propertyDisableMethod, disableForContextFacet.getMethods().getFirstElseFail().asMethodElseFail());
        });
    }

}
