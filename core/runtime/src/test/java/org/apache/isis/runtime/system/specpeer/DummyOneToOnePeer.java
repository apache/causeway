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


@SuppressWarnings("unchecked")
public class DummyOneToOnePeer extends FacetHolderImpl implements ObjectAssociationPeer {
	
    private final ExpectedSet expectedActions = new ExpectedSet();
    Vector actions = new Vector();
    public ObjectAdapter getObject;
    public boolean isEmpty;
    public boolean isVisible;

	public void clearAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        actions.addElement("clear " + inObject);
        actions.addElement("clear " + associate);
    }

    public void expect(final String string) {
        expectedActions.addExpected(string);
    }

    public ObjectAdapter getAssociation(final ObjectAdapter inObject) {
        actions.addElement("get " + inObject);
        return getObject;
    }

    public ObjectAdapter getDefault(final ObjectAdapter target) {
        return null;
    }

    public Object[] getOptions(final ObjectAdapter target) {
        return null;
    }

    public Identifier getIdentifier() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {}

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

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        actions.addElement("associate " + inObject);
        actions.addElement("associate " + associate);
    }

    public void verify() {
        expectedActions.verify();
    }

    public void assertAction(final int index, final String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter value) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isVisibleDeclaratively() {
        return true;
    }

    public boolean isVisibleForSession(final AuthenticationSession session) {
        return true;
    }

    public boolean isVisible(final ObjectAdapter target) {
        return isVisible;
    }

    public void setupVisible(final boolean isVisible) {
        this.isVisible = isVisible;
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

    public boolean isOptionEnabled() {
        return false;
    }

    public String getHelp() {
        return null;
    }

    public void debugData(final DebugString debugString) {}

    public String getBusinessKeyName() {
        return null;
    }

    public boolean isOneToMany() {
        return false;
    }

    public boolean isOneToOne() {
        return true;
    }

}
