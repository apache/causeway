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


package org.apache.isis.progmodel.java5.reflect;

import java.lang.reflect.Method;

import org.apache.isis.noa.adapter.[[NAME]];
import org.apache.isis.metamodel.facets.When;
import org.apache.isis.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet.Where;
import org.apache.isis.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.metamodel.facets.disable.DisabledFacet;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectAction;
import org.apache.isis.noa.spec.ObjectSpecification;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.progmodel.java5.facets.actions.invoke.JavaActionInvocationFacet;
import org.apache.isis.progmodel.java5.facets.actions.validate.ActionValidMethodFacet;
import org.apache.isis.progmodel.java5.reflect.actions.JavaAction;
import org.apache.isis.progmodel.java5.reflect.actions.JavaActionParam;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.testsystem.ProxyTestCase;
import org.apache.isis.nof.testsystem.TestProxyAdapter;


public class JavaActionTest extends ProxyTestCase {
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(JavaActionTest.class);
    }

    private JavaAction javaAction;
    private TestProxyAdapter target;
    private JavaActionTestObject javaObject;

    protected void setUp() throws Exception {
        super.setUp();

        javaObject = new JavaActionTestObject();
        target = system.createAdapterForTransient(javaObject);

        MemberIdentifierImpl memberIdentifierImpl = new MemberIdentifierImpl("cls", "methodName",
                (ObjectSpecification[]) null);
        javaAction = new JavaAction(memberIdentifierImpl, null, null, new JavaActionParam[0]);
        assertNotNull(javaAction);

        Class cls = JavaActionTestObject.class;
        Method action = cls.getDeclaredMethod("actionMethod", new Class[0]);
        javaAction.addFacet(new JavaActionInvocationFacet(action, null, javaAction));

        Method valid = cls.getDeclaredMethod("validMethod", new Class[0]);
        javaAction.addFacet(new ActionValidMethodFacet(valid, javaAction));

    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testAction() throws Exception {
        javaAction.execute(target, new [[NAME]][0]);
        assertTrue(javaObject.actionCalled());
    }

    public void testReturnType() {
        assertNull(javaAction.getReturnType());
    }

    public void testTypeUserByDefault() {
        assertEquals(ObjectAction.USER, javaAction.getType());
    }

    public void testTypeExploration() {
        javaAction.addFacet(new ExplorationFacet(javaAction));
        assertEquals(ObjectAction.EXPLORATION, javaAction.getType());
    }

    public void testTypeDebug() {
        javaAction.addFacet(new DebugFacet(javaAction));
        assertEquals(ObjectAction.DEBUG, javaAction.getType());
    }

    public void testTargetDefault() {
        javaAction.addFacet(new DebugFacet(javaAction));
        assertEquals(ObjectAction.DEFAULT, javaAction.getTarget());
    }

    public void testTargetLocal() {
        javaAction.addFacet(new ExecutedFacet(Where.LOCALLY, javaAction));
        assertEquals(ObjectAction.LOCAL, javaAction.getTarget());
    }

    public void testTargetRemote() {
        javaAction.addFacet(new ExecutedFacet(Where.REMOTELY, javaAction));
        assertEquals(ObjectAction.REMOTE, javaAction.getTarget());
    }

    public void testUsableByDefault() {
        assertEquals(Allow.DEFAULT, javaAction.isUsableDeclaratively());
    }

    public void testUsableDeclaratively() {
        javaAction.addFacet(new DisabledFacet(When.ALWAYS, javaAction));
        assertTrue(javaAction.isUsableDeclaratively().isVetoed());
    }

    public void testUsableInContextByDefault() {
        assertEquals(Allow.DEFAULT, javaAction.isUsable(target));
    }

    public void testTransientObjectDisabled() {
        javaAction.addFacet(new DisabledFacet(When.UNTIL_PERSISTED, javaAction));
        assertTrue(javaAction.isUsable(target).isVetoed());
    }

    public void testTransientObjectEnabled() {
        javaAction.addFacet(new DisabledFacet(When.ONCE_PERSISTED, javaAction));
        assertEquals(Allow.DEFAULT, javaAction.isUsable(target));
    }

    public void testPersistentObjectDisabled() {
        system.makePersistent(target);
        javaAction.addFacet(new DisabledFacet(When.ONCE_PERSISTED, javaAction));
        assertTrue(javaAction.isUsable(target).isVetoed());
    }

    public void testPesistentObjectEnabled() {
        system.makePersistent(target);
        javaAction.addFacet(new DisabledFacet(When.UNTIL_PERSISTED, javaAction));
        assertEquals(Allow.DEFAULT, javaAction.isUsable(target));
    }

    public void testParameterSetValid() throws Exception {
        Consent consent = javaAction.isParameterSetValidImperatively(target, new [[NAME]][0]);
        assertEquals(false, consent.isAllowed());
        assertEquals("invalid", consent.getReason());
    }
}
