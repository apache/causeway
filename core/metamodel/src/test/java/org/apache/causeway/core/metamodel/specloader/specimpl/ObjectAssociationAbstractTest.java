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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

@ExtendWith(MockitoExtension.class)
class ObjectAssociationAbstractTest {

    @Mock private ObjectSpecification objectSpecification;
    @Mock private ServiceInjector mockServicesInjector;
    @Mock private SpecificationLoader mockSpecificationLoader;

    private ObjectAssociationAbstract objectAssociation;
    private FacetedMethod facetedMethod;


    public static class Customer {
        private String firstName;

        public String getFirstName() {
            return firstName;
        }
    }

    @BeforeEach
    public void setup() {

        MetaModelContext mmc = MetaModelContext_forTesting.buildDefault();
        facetedMethod = FacetedMethod.createForProperty(mmc , Customer.class, "firstName");

        objectAssociation = new ObjectAssociationAbstract(
                facetedMethod.getFeatureIdentifier(),
                facetedMethod, FeatureType.PROPERTY, objectSpecification) {

            @Override
            public ManagedObject get(
                    final ManagedObject fromObject,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }

            @Override
            public boolean isEmpty(final ManagedObject adapter, final InteractionInitiatedBy interactionInitiatedBy) {
                return false;
            }

            @Override
            public Can<ManagedObject> getChoices(
                    final ManagedObject object,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }

            @Override
            public ManagedObject getDefault(final ManagedObject adapter) {
                return null;
            }

            @Override
            public void toDefault(final ManagedObject target) {
            }

            @Override
            public UsabilityContext createUsableInteractionContext(
                    final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy,
                    final Where where) {
                return null;
            }

            @Override
            public VisibilityContext createVisibleInteractionContext(
                    final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
                    final Where where) {
                return null;
            }

            @Override
            public boolean containsNonFallbackFacet(final Class<? extends Facet> facetType) {
                return false;
            }

            @Override
            public boolean hasAutoComplete() {
                return false;
            }

            @Override
            public Can<ManagedObject> getAutoComplete(
                    final ManagedObject object,
                    final String searchArg,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }
            @Override
            public int getAutoCompleteMinLength() {
                return 0;
            }

            @Override
            public boolean isExplicitlyAnnotated() {
                return false;
            }

        };
    }

    @Test
    public void notHidden() throws Exception {
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void optional() throws Exception {
        assertFalse(objectAssociation.isMandatory());
    }

    @Test
    public void mandatory() throws Exception {
        final MandatoryFacet mockFacet = mockFacetIgnoring(MandatoryFacet.class, Precedence.DEFAULT);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isMandatory());
    }

    @Test
    public void hasNoChoices() throws Exception {
        assertFalse(objectAssociation.hasChoices());
    }

    @Test
    public void hasChoices() throws Exception {
        final PropertyChoicesFacet mockFacet = mockFacetIgnoring(PropertyChoicesFacet.class, Precedence.DEFAULT);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.hasChoices());
    }

    private <T extends Facet> T mockFacetIgnoring(final Class<T> typeToMock, final Precedence precedence) {
        final T facet = Mockito.mock(typeToMock);
        Mockito.when(facet.facetType()).thenReturn(_Casts.uncheckedCast(typeToMock));
        Mockito.when(facet.getPrecedence()).thenReturn(precedence);
        return facet;
    }
}
