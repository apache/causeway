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

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.TypedHolder;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;


public class DummyCollectionPeer extends FacetHolderImpl implements ObjectMemberPeer {

    Vector<String> actions = new Vector<String>();
    private final ExpectedSet expectedActions = new ExpectedSet();
    public ObjectAdapter getCollection;
    public boolean isEmpty;
    public String label;
    // String name;
    private final TestProxySpecification specification;

    public DummyCollectionPeer(final TestProxySpecification specification) {
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

    @Override
    public void debugData(final DebugString debugString) {}

    public void expect(final String string) {
        expectedActions.addExpected(string);
    }

    public ObjectAdapter getAssociations(final ObjectAdapter inObject) {
        actions.addElement("get " + inObject);
        return getCollection;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.classIdentifier("SomeClassName");
    }


    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public void setType(Class<?> type) {
    }

    @Override
    public ObjectSpecification getSpecification(final SpecificationLoader specificationLoader) {
        return specification;
    }

    public boolean isEmpty(final ObjectAdapter inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
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

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.COLLECTION;
    }

    @Override
    public List<TypedHolder> getChildren() {
        return Collections.emptyList();
    }

}
