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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
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
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

public class ObjectMemberAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    private ObjectMemberAbstractImpl testMember;
    
    private ObjectAdapter persistentAdapter;
    private ObjectAdapter transientAdapter;
    
    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        persistentAdapter = PojoAdapterBuilder.create().build();
        transientAdapter = PojoAdapterBuilder.create().with(Persistence.TRANSIENT).build();

        testMember = new ObjectMemberAbstractImpl("id");
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
        final Consent usable = testMember.isUsable(null, persistentAdapter, Where.ANYWHERE);
        final boolean allowed = usable.isAllowed();
        assertTrue(allowed);
    }

    @Test
    public void testVisibleWhenHiddenFacetSetToAlways() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstract(When.ALWAYS, Where.ANYWHERE, testMember) {
            @Override
            public String hiddenReason(final ObjectAdapter target, final Where whereContext) {
                return null;
            }
        });
        final Consent visible = testMember.isVisible(null, persistentAdapter, Where.ANYWHERE);
        assertTrue(visible.isAllowed());
    }

    @Test
    public void testVisibleWhenTargetPersistentAndHiddenFacetSetToOncePersisted() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstractImpl(When.ONCE_PERSISTED, Where.ANYWHERE, testMember){});
        assertFalse(testMember.isVisible(null, persistentAdapter, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testVisibleWhenTargetPersistentAndHiddenFacetSetToUntilPersisted() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstractImpl(When.UNTIL_PERSISTED, Where.ANYWHERE, testMember){});
        final Consent visible = testMember.isVisible(null, persistentAdapter, Where.ANYWHERE);
        assertTrue(visible.isAllowed());
    }

    @Test
    public void testVisibleWhenTargetTransientAndHiddenFacetSetToUntilPersisted() {
        testMember.addFacet(new HideForContextFacetNone(testMember));
        testMember.addFacet(new HiddenFacetAbstractImpl(When.UNTIL_PERSISTED, Where.ANYWHERE, testMember){});
        
        final Consent visible = testMember.isVisible(null, transientAdapter, Where.ANYWHERE);
        assertFalse(visible.isAllowed());
    }

    @Test
    public void testVisibleDeclaratively() {
        testMember.addFacet(new HiddenFacetAbstractAlwaysEverywhere(testMember) {});
        assertFalse(testMember.isVisible(null, persistentAdapter, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testVisibleForSessionByDefault() {
        final Consent visible = testMember.isVisible(null, persistentAdapter, Where.ANYWHERE);
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
        assertFalse(testMember.isVisible(null, persistentAdapter, Where.ANYWHERE).isAllowed());
    }

    @Test
    public void testVisibleForSessionFails() {
        testMember.addFacet(new HideForSessionFacetAbstract(testMember) {
            @Override
            public String hiddenReason(final AuthenticationSession session) {
                return "hidden";
            }
        });
        assertFalse(testMember.isVisible(null, persistentAdapter, Where.ANYWHERE).isAllowed());
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

    protected ObjectMemberAbstractImpl(final String id) {
        super(FacetedMethod.createForProperty(Customer.class, "firstName"), FeatureType.PROPERTY, new ObjectMemberContext(DeploymentCategory.PRODUCTION, null, null, null, null, null));
    }

    @Override
    public String debugData() {
        return null;
    }

    public Consent isUsable(final ObjectAdapter target) {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter target, Where where) {
        return new PropertyUsabilityContext(DeploymentCategory.PRODUCTION, session, invocationMethod, target, getIdentifier(), where);
    }

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObjectAdapter, Where where) {
        return new PropertyVisibilityContext(DeploymentCategory.PRODUCTION, session, invocationMethod, targetObjectAdapter, getIdentifier(), where);
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        return null;
    }

}
