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

package org.apache.isis.core.runtime.system;

import org.datanucleus.enhancement.Persistable;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.forsession.DisableForSessionFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstractAlwaysEverywhere;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstractImpl;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetNone;
import org.apache.isis.core.metamodel.interactions.PropertyUsabilityContext;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ObjectMemberAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    private ObjectMemberAbstractImpl testMember;
    
    private ObjectAdapter persistentAdapter;
    private ObjectAdapter transientAdapter;

    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;
    @Mock
    private AuthenticationSession mockAuthenticationSession;
    @Mock
    private SpecificationLoader mockSpecificationLoader;

    ServicesInjector stubServicesInjector;

    @Mock
    private ObjectSpecification mockSpecForCustomer;

    @Mock
    private Persistable mockPersistable;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        stubServicesInjector = ServicesInjector.builderForTesting()
                .addServices(_Lists.<Object>of(
                        mockSpecificationLoader, 
                        mockPersistenceSessionServiceInternal))
                .build(); 

        context.checking(new Expectations() {{
            allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
            will(returnValue(mockAuthenticationSession));
        }});
//        persistentAdapter = PojoAdapterBuilder.create()
//                .with(mockSpecificationLoader)
//                .withOid("CUS|1")
//                .withPojo(mockPersistable)
//                .build();

        persistentAdapter = PojoAdapter.of(
                mockPersistable,
                Factory.persistentOf(ObjectSpecId.of("CUS"), "1"),
                mockAuthenticationSession,
                mockSpecificationLoader,
                null);


        transientAdapter = PojoAdapterBuilder.create()
                .with(mockSpecificationLoader)
                .with(Persistence.TRANSIENT)
                .withPojo(mockPersistable)
                .build();

        testMember = new ObjectMemberAbstractImpl("id", stubServicesInjector);

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).lookupBySpecId(ObjectSpecId.of("CUS"));
            will(returnValue(mockSpecForCustomer));
            allowing(mockSpecificationLoader).loadSpecification(with(any(Class.class)));
            will(returnValue(mockSpecForCustomer));

            allowing(mockSpecForCustomer).isService();
            will(returnValue(false));

            allowing(mockSpecForCustomer).isViewModel();
            will(returnValue(false));

            allowing(mockSpecForCustomer).getShortIdentifier();
            will(returnValue("Customer"));
        }});

    }

    @Test
    public void testToString() throws Exception {
        testMember.addFacet(new NamedFacetAbstract("", true, testMember) {});
        assertThat(testMember.toString(), not(isEmptyString()));
    }

    @Test
    public void testAvailableForUser() throws Exception {
        testMember.addFacet(new DisableForSessionFacetAbstract(testMember) {
            @Override
            public String disabledReason(final AuthenticationSession session) {
                return null;
            }
        });
        final Consent usable = testMember.isUsable(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
        final boolean allowed = usable.isAllowed();
        assertTrue(allowed);
    }

    @Test
    public void testVisibleWhenHiddenFacetSetToAlways() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstract(Where.ANYWHERE, testMember) {
            @Override
            public String hiddenReason(final ManagedObject target, final Where whereContext) {
                return null;
            }
        });
        final Consent visible = testMember.isVisible(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
        assertTrue(visible.isAllowed());
    }

    @Test
    public void testVisibleWhenHiddenFacetSet() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstractImpl(Where.ANYWHERE, testMember){});

        final Consent visible = testMember.isVisible(transientAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
        assertFalse(visible.isAllowed());
    }

    @Test
    public void testVisibleDeclaratively() {
        testMember.addFacet(new HiddenFacetAbstractAlwaysEverywhere(testMember) {});
        assertFalse(testMember.isVisible(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testVisibleForSessionByDefault() {
        final Consent visible = testMember.isVisible(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);
        assertTrue(visible.isAllowed());
    }

    @Test
    public void testVisibleForSession() {
        testMember.addFacet(new HideForSessionFacetAbstract(testMember) {
            @Override
            public String hiddenReason(final AuthenticationSession session) {
                return "Hidden";
            }
        });
        assertFalse(testMember.isVisible(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testVisibleForSessionFails() {
        testMember.addFacet(new HideForSessionFacetAbstract(testMember) {
            @Override
            public String hiddenReason(final AuthenticationSession session) {
                return "hidden";
            }
        });
        assertFalse(testMember.isVisible(persistentAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testName() throws Exception {
        final String name = "action name";
        testMember.addFacet(new NamedFacetAbstract(name, true, testMember) {
        });
        assertThat(testMember.getName(), is(equalTo(name)));
    }

    @Test
    public void testDescription() throws Exception {
        final String name = "description text";
        testMember.addFacet(new DescribedAsFacetAbstract(name, testMember) {
        });
        assertEquals(name, testMember.getDescription());
    }
}

class ObjectMemberAbstractImpl extends ObjectMemberAbstract {

    public static class Customer {
        private String firstName;

        public String getFirstName() {
            return firstName;
        }
    }

    protected ObjectMemberAbstractImpl(final String id, final ServicesInjector servicesInjector) {
        super(FacetedMethod.createForProperty(Customer.class, "firstName"), FeatureType.PROPERTY);
    }

    /**
     * @deprecated - unused ?
     */
    @Deprecated
    public Consent isUsable(final ObjectAdapter target) {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyUsabilityContext(target, getIdentifier(), interactionInitiatedBy, where);
    }

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyVisibilityContext(targetObjectAdapter, getIdentifier(), interactionInitiatedBy,
                where);
    }


}
