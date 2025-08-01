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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
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

class PropertyMethodsFacetFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void propertyAccessorFacetIsInstalledAndMethodRemoved() {
        var facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        final ResolvedMethod propertyAccessorMethod = findMethodExactOrFail(Customer.class, "getFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
            var propertyAccessorFacetViaAccessor = (PropertyAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(propertyAccessorMethod, propertyAccessorFacetViaAccessor);
            assertMethodWasRemoved(propertyAccessorMethod);
        });
    }

    @Test
    void setterFacetIsInstalledForSetterMethodAndMethodRemoved() {
        var facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) { }
        }
        final ResolvedMethod propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertySetterFacetViaSetterMethod);
            var propertySetterFacet = (PropertySetterFacetViaSetterMethod) facet;
            assertMethodEqualsFirstIn(propertySetterMethod, propertySetterFacet);
            assertMethodWasRemoved(propertySetterMethod);
        });
    }

    @Test
    void initializationFacetIsInstalledForSetterMethodAndMethodRemoved() {
        var facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) {}
        }
        final ResolvedMethod propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyInitializationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyInitializationFacet);
            var propertySetterFacet = (PropertyInitializationFacetViaSetterMethod) facet;
            assertMethodEqualsFirstIn(propertySetterMethod, propertySetterFacet);
            assertMethodWasRemoved(propertySetterMethod);
        });
    }

    @Test
    void setterFacetIsInstalledMeansNoDisabledOrDerivedFacetsInstalled() {
        var facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) {}
        }
        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            assertNull(facetedMethod.getFacet(SnapshotExcludeFacet.class));
        });
    }

    @Test
    void clearFacetViaSetterIfNoExplicitClearMethod() {
        var facetFactory = new PropertySetterFacetFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public void setFirstName(final String firstName) { }
        }
        final ResolvedMethod propertySetterMethod = findMethodExactOrFail(Customer.class, "setFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyClearFacetViaSetterMethod);
            var propertyClearFacet = (PropertyClearFacetViaSetterMethod) facet;
            assertMethodEqualsFirstIn(propertySetterMethod, propertyClearFacet);
        });
    }

    @Test
    void choicesFacetFoundAndMethodRemoved() {
        var facetFactory = new PropertyChoicesFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String[] choicesFirstName() { return null; }
        }

        final ResolvedMethod propertyChoicesMethod = findMethodExactOrFail(Customer.class, "choicesFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyChoicesFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyChoicesFacetViaMethod);
            var propertyChoicesFacet = (PropertyChoicesFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyChoicesMethod, propertyChoicesFacet);
            assertMethodWasRemoved(propertyChoicesMethod);
        });
    }

    @Test
    void autoCompleteFacetFoundAndMethodRemoved() {
        var facetFactory = new PropertyAutoCompleteFacetMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String[] autoCompleteFirstName(final String searchArg) { return null; }
        }

        final ResolvedMethod propertyAutoCompleteMethod = findMethodExactOrFail(Customer.class, "autoCompleteFirstName", new Class[]{String.class});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyAutoCompleteFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAutoCompleteFacetMethod);
            var propertyAutoCompleteFacet = (PropertyAutoCompleteFacetMethod) facet;
            assertMethodEqualsFirstIn(propertyAutoCompleteMethod, propertyAutoCompleteFacet);
            assertMethodWasRemoved(propertyAutoCompleteMethod);
        });
    }

    @Test
    void defaultFacetFoundAndMethodRemoved() {
        var facetFactory = new PropertyDefaultFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String defaultFirstName() { return null; }
        }

        final ResolvedMethod propertyDefaultMethod = findMethodExactOrFail(Customer.class, "defaultFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyDefaultFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyDefaultFacetViaMethod);
            var propertyDefaultFacet = (PropertyDefaultFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyDefaultMethod, propertyDefaultFacet);
            assertMethodWasRemoved(propertyDefaultMethod);
        });
    }

    @Test
    void validateFacetFoundAndMethodRemoved() {
        var facetFactory = new PropertyValidateFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String validateFirstName(final String firstName) { return null;}
        }

        final ResolvedMethod propertyValidateMethod = findMethodExactOrFail(Customer.class, "validateFirstName", new Class[] { String.class });

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyValidateFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyValidateFacetViaMethod);
            var propertyValidateFacet = (PropertyValidateFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyValidateMethod, propertyValidateFacet);
            assertMethodWasRemoved(propertyValidateMethod);
        });
    }

    @Test
    void disableFacetFoundAndMethodRemoved() {
        var facetFactory = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String disableFirstName() { return "disabled"; }
        }

        final ResolvedMethod propertyDisableMethod = findMethodExactOrFail(Customer.class, "disableFirstName", new Class[] {});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DisableForContextFacetViaMethod);
            var disableForContextFacet = (DisableForContextFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyDisableMethod, disableForContextFacet);
            assertMethodWasRemoved(propertyDisableMethod);
        });
    }

    @Test
    void disableFacetNoArgsFoundAndMethodRemoved() {
        var facetFactory = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public String disableFirstName() { return "disabled"; }
        }

        final ResolvedMethod propertyDisableMethod = findMethodExactOrFail(Customer.class, "disableFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof DisableForContextFacetViaMethod);
            var disableForContextFacet = (DisableForContextFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyDisableMethod, disableForContextFacet);
            assertMethodWasRemoved(propertyDisableMethod);
        });
    }

    @Test
    void hiddenFacetFoundAndMethodRemoved() {
        var facetFactory = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public boolean hideFirstName() { return true; }
        }

        final ResolvedMethod propertyHideMethod = findMethodExactOrFail(Customer.class, "hideFirstName", new Class[] {});

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            var hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyHideMethod, hideForContextFacet);
            assertMethodWasRemoved(propertyHideMethod);
        });
    }

    @Test
    void hiddenFacetWithNoArgFoundAndMethodRemoved() {
        var facetFactory = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
            public boolean hideFirstName() { return true; }
        }

        final ResolvedMethod propertyHideMethod = findMethodExactOrFail(Customer.class, "hideFirstName");

        propertyScenario(Customer.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            var hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyHideMethod, hideForContextFacet);
            assertMethodWasRemoved(propertyHideMethod);
        });
    }

    @Test
    void propertyFoundOnSuperclass() {
        var facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        class CustomerEx extends Customer {
        }

        final ResolvedMethod propertyAccessorMethod = findMethodExactOrFail(CustomerEx.class, "getFirstName");

        propertyScenario(CustomerEx.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(PropertyOrCollectionAccessorFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PropertyAccessorFacetViaAccessor);
            var accessorFacet = (PropertyAccessorFacetViaAccessor) facet;
            assertMethodEqualsFirstIn(propertyAccessorMethod, accessorFacet);
        });

    }

    @Test
    void propertyFoundOnSuperclassButHelperMethodFoundOnSubclass() {
        var facetFactory = new PropertyAccessorFacetViaAccessorFactory(getMetaModelContext());
        var facetFactoryForHide = new HideForContextFacetViaMethodFactory(getMetaModelContext());
        var facetFactoryForDisable = new DisableForContextFacetViaMethodFactory(getMetaModelContext());
        @SuppressWarnings("unused")
        class Customer {
            public String getFirstName() { return null; }
        }
        @SuppressWarnings("unused")
        class CustomerEx extends Customer {
            public boolean hideFirstName() { return true; }
            public String disableFirstName() { return "disabled";}
        }

        final ResolvedMethod propertyHideMethod = findMethodExactOrFail(CustomerEx.class, "hideFirstName");
        final ResolvedMethod propertyDisableMethod = findMethodExactOrFail(CustomerEx.class, "disableFirstName");

        propertyScenario(CustomerEx.class, "firstName", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            facetFactory.process(processMethodContext);
            facetFactoryForHide.process(processMethodContext);
            facetFactoryForDisable.process(processMethodContext);

            final Facet facet = facetedMethod.getFacet(HideForContextFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HideForContextFacetViaMethod);
            var hideForContextFacet = (HideForContextFacetViaMethod) facet;
            assertMethodEqualsFirstIn(propertyHideMethod, hideForContextFacet);

            final Facet facet2 = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet2);
            assertTrue(facet2 instanceof DisableForContextFacetViaMethod);
            var disableForContextFacet = (DisableForContextFacetViaMethod) facet2;
            assertMethodEqualsFirstIn(propertyDisableMethod, disableForContextFacet);
        });
    }

}
