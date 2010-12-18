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


package org.apache.isis.core.progmodel.facets.ordering.memberorder;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.NamedAndDescribedFacetHolderImpl;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.TypedHolder;


final class MemberPeerStub extends NamedAndDescribedFacetHolderImpl implements ObjectMemberPeer {

    public static MemberPeerStub createProperty(final String name) {
        return new MemberPeerStub(FeatureType.PROPERTY, name);
    }

    public static MemberPeerStub createCollection(final String name) {
        return new MemberPeerStub(FeatureType.COLLECTION, name);
    }

    public static MemberPeerStub createAction(final String name) {
        return new MemberPeerStub(FeatureType.ACTION, name);
    }

    private final FeatureType featureType;

    private MemberPeerStub(final FeatureType collection, final String name) {
        super(name);
        this.featureType = collection;
    }


    @Override
    public void debugData(final DebugString debugString) {}

    public String getHelp() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.classIdentifier(MemberPeerStub.this.getName());
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
        return false;
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
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer#getChildren()
     */
    @Override
    public List<TypedHolder> getChildren() {
        return Collections.emptyList();
    }

}
