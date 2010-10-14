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


package org.apache.isis.metamodel.runtimecontext.spec.feature;

import org.junit.Before;
import org.junit.Test;
import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.metamodel.facets.hide.HiddenFacetAlways;
import org.apache.isis.metamodel.facets.propcoll.derived.DerivedFacetInferred;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacetAnnotation;
import org.apache.isis.metamodel.facets.properties.choices.PropertyChoicesFacetAbstract;
import org.apache.isis.metamodel.facets.propparam.validate.mandatory.MandatoryFacetDefault;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.runtimecontext.spec.feature.ObjectMemberAbstract.MemberType;
import org.apache.isis.metamodel.spec.identifier.IdentifiedImpl;
import org.apache.isis.metamodel.testspec.TestProxySpecification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ObjectAssociationAbstractTest {

    private ObjectAssociationAbstract objectAssociation;
    private IdentifiedImpl facetHolder;

    @Before
    public void setup() {
        facetHolder = new IdentifiedImpl();
        objectAssociation = new ObjectAssociationAbstract("id", new TestProxySpecification("test"),
                MemberType.ONE_TO_ONE_ASSOCIATION, facetHolder, null) {

            public ObjectAdapter get(ObjectAdapter fromObject) {
                return null;
            }

            public boolean isEmpty(ObjectAdapter adapter) {
                return false;
            }

            public ObjectAdapter[] getChoices(ObjectAdapter object) {
                return null;
            }

            public ObjectAdapter getDefault(ObjectAdapter adapter) {
                return null;
            }

            public void toDefault(ObjectAdapter target) {}

            public UsabilityContext<?> createUsableInteractionContext(
                    AuthenticationSession session,
                    InteractionInvocationMethod invocationMethod,
                    ObjectAdapter target) {
                return null;
            }

            public VisibilityContext<?> createVisibleInteractionContext(
                    AuthenticationSession session,
                    InteractionInvocationMethod invocationMethod,
                    ObjectAdapter targetObjectAdapter) {
                return null;
            }

            public String debugData() {
                return null;
            }

            public Instance getInstance(ObjectAdapter adapter) {
                return null;
            }
        };
    }

    @Test
    public void notPersistedWhenDerived() throws Exception {
        facetHolder.addFacet(new DerivedFacetInferred(facetHolder));
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void notPersistedWhenFlaggedAsNotPersisted() throws Exception {
        facetHolder.addFacet(new NotPersistedFacetAnnotation(facetHolder));
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void persisted() throws Exception {
        assertFalse(objectAssociation.isNotPersisted());
    }

    @Test
    public void notHidden() throws Exception {
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void hidden() throws Exception {
        facetHolder.addFacet(new HiddenFacetAlways(facetHolder));
        assertTrue(objectAssociation.isAlwaysHidden());
    }
    
    @Test
    public void optional() throws Exception {
        assertFalse(objectAssociation.isMandatory());
    }

    @Test
    public void mandatory() throws Exception {
        facetHolder.addFacet(new MandatoryFacetDefault(facetHolder));
        assertTrue(objectAssociation.isMandatory());
    }

    @Test
    public void hasNoChoices() throws Exception {
        assertFalse(objectAssociation.hasChoices());
    }

    @Test
    public void hasChoices() throws Exception {
        facetHolder.addFacet(new PropertyChoicesFacetAbstract(facetHolder) {
            public Object[] getChoices(ObjectAdapter adapter) {
                return null;
            }
        });
        assertTrue(objectAssociation.hasChoices());
    }
}

