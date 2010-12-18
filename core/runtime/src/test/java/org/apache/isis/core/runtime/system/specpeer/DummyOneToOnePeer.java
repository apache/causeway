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


package org.apache.isis.core.runtime.system.specpeer;

import java.util.Vector;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;


@SuppressWarnings("unchecked")
public class DummyOneToOnePeer extends FacetHolderImpl implements ObjectMemberPeer {
	
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

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification(final SpecificationLoader specificationLoader) {
        return null;
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {}

    public boolean isEmpty(final ObjectAdapter inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
    }

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        actions.addElement("associate " + inObject);
        actions.addElement("associate " + associate);
    }

    public void verify() {
        expectedActions.verify();
    }

    @Override
    public void debugData(final DebugString debugString) {}

    protected MemberType getMemberType() {
        return MemberType.PROPERTY;
    }

    @Override
    public boolean isProperty() {
        return getMemberType().isProperty();
    }

    @Override
    public boolean isCollection() {
        return getMemberType().isCollection();
    }

    @Override
    public boolean isAction() {
        return getMemberType().isAction();
    }

}
