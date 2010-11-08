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

import java.util.Vector;

import junit.framework.Assert;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Allow;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.facets.FacetHolderImpl;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectAssociationPeer;
import org.apache.isis.metamodel.testspec.TestProxySpecification;


public class DummyOneToManyPeer extends FacetHolderImpl implements ObjectAssociationPeer {

    Vector<String> actions = new Vector<String>();
    private final ExpectedSet expectedActions = new ExpectedSet();
    public ObjectAdapter getCollection;
    public boolean isEmpty;
    public String label;
    // String name;
    private final TestProxySpecification specification;

    public DummyOneToManyPeer(final TestProxySpecification specification) {
        this.specification = specification;
    }

	public void addAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        actions.addElement("add " + inObject);
        actions.addElement("add " + associate);
    }

    public void assertAction(final int index, final String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public void assertActions(final int noOfActions) {
        if (noOfActions != actions.size()) {
            Assert.fail("Expected " + noOfActions + ", but got " + actions.size());
        }
    }

    public void debugData(final DebugString debugString) {}

    public void expect(final String string) {
        expectedActions.addExpected(string);
    }

    public ObjectAdapter getAssociations(final ObjectAdapter inObject) {
        actions.addElement("get " + inObject);
        return getCollection;
    }

    public String getBusinessKeyName() {
        return null;
    }

    public ObjectAdapter getDefault(final ObjectAdapter target) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public Identifier getIdentifier() {
        return Identifier.classIdentifier("SomeClassName");
    }

    public String getName() {
        return null;
    }

    public Object[] getOptions(final ObjectAdapter target) {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return specification;
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {}

    public void initOneToManyAssociation(final ObjectAdapter inObject, final ObjectAdapter[] instances) {}

    public Consent isAddValid(final ObjectAdapter container, final ObjectAdapter element) {
        return null;
    }

    public boolean isPersisted() {
        return true;
    }

    public boolean isEmpty(final ObjectAdapter inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
    }

    public boolean isMandatory() {
        return false;
    }

    public Consent isRemoveValid(final ObjectAdapter container, final ObjectAdapter element) {
        return null;
    }

    public Consent isUsableDeclaratively() {
        return Allow.DEFAULT;
    }

    public Consent isUsableForSession(final AuthenticationSession session) {
        return Allow.DEFAULT;
    }

    public Consent isUsable(final ObjectAdapter target) {
        return null;
    }

    public boolean isVisibleDeclaratively() {
        return false;
    }

    public boolean isVisibleForSession(final AuthenticationSession session) {
        return false;
    }

    public boolean isVisible(final ObjectAdapter target) {
        return true;
    }

    public void removeAllAssociations(final ObjectAdapter inObject) {
        actions.addElement("removeall " + inObject);
    }

    public void removeAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        actions.addElement("remove " + inObject);
        actions.addElement("remove " + associate);
    }

    public void verify() {
        expectedActions.verify();
    }

    public boolean isOneToMany() {
        return true;
    }

    public boolean isOneToOne() {
        return false;
    }

}
