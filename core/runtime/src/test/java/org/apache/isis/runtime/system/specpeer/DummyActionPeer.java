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


package org.apache.isis.runtime.system.specpeer;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Allow;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.facets.FacetHolderImpl;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Target;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectActionParamPeer;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectActionPeer;
import org.apache.isis.runtime.testsystem.TestSpecification;



public final class DummyActionPeer extends FacetHolderImpl implements ObjectActionPeer {

    private final ExpectedSet expectedActions = new ExpectedSet();
    private String name;
    private ObjectSpecification[] paramterTypes;
    private ObjectAdapter returnObject;
    private ObjectSpecification returnType;

    public void debugData(final DebugString debugString) {}

    public ObjectAdapter execute(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        expectedActions.addActual("execute " + getIdentifier() + " " + object);
        return returnObject;
    }

    public void expect(final String string) {
        expectedActions.addExpected(string);
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public Identifier getIdentifier() {
        return Identifier.classIdentifier(name);
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("deprecation")
    public ObjectSpecification getOnType() {
        return new TestSpecification();
    }

    public int getParameterCount() {
        return 3;
    }

    public Object[] getParameterDefaults(final ObjectAdapter target) {
        return new Object[] { "", Integer.valueOf(123), null };
    }

    public String[] getParameterDescriptions() {
        return new String[] { "description one", "description two", "description three" };
    }

    public String[] getParameterNames() {
        return new String[] { "one", "two", "three" };
    }

    public Object[][] getParameterOptions(final ObjectAdapter target) {
        return new Object[][] { { "test", "the", "options" }, null, null };
    }

    public ObjectSpecification[] getParameterTypes() {
        return paramterTypes;
    }

    public boolean[] getOptionalParameters() {
        return new boolean[3];
    }

    public ObjectSpecification getReturnType() {
        return returnType;
    }

    public Target getTarget() {
        return null;
    }

    public ObjectActionType getType() {
        return null;
    }

    public boolean isVisibleDeclaratively() {
        return true;
    }

    public boolean isVisibleForSession(final AuthenticationSession session) {
        return true;
    }

    public boolean isVisible(final ObjectAdapter target) {
        return true;
    }

    public Consent isUsableDeclaratively() {
        return Allow.DEFAULT;
    }

    public Consent isUsableForSession(final AuthenticationSession session) {
        return Allow.DEFAULT;
    }

    public Consent isUsable(final ObjectAdapter target) {
        return Allow.DEFAULT;
    }

    public boolean isOnInstance() {
        return true;
    }

    public Consent isParameterSetValidImperatively(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        return null;
    }

    public void setupName(final String name) {
        this.name = name;
    }

    public void setUpParamterTypes(final ObjectSpecification[] paramterTypes) {
        this.paramterTypes = paramterTypes;
    }

    public void setupReturnObject(final ObjectAdapter returnObject) {
        this.returnObject = returnObject;
    }

    public void setupReturnType(final ObjectSpecification returnType) {
        this.returnType = returnType;
    }

    public void verify() {
        expectedActions.verify();
    }

    public boolean[] canParametersWrap() {
        return new boolean[3];
    }

    public int[] getParameterMaxLengths() {
        return new int[3];
    }

    public int[] getParameterNoLines() {
        return new int[3];
    }

    public int[] getParameterTypicalLengths() {
        return new int[3];
    }

    public ObjectActionParamPeer[] getParameters() {
        return new ObjectActionParamPeer[] { new DummyActionParamPeer(), new DummyActionParamPeer(),
                new DummyActionParamPeer(), };
    }

}
