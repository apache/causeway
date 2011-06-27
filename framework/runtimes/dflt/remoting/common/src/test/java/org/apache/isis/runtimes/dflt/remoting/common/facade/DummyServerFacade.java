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

package org.apache.isis.runtimes.dflt.remoting.common.facade;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationRequestUsability;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationRequestVisibility;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearAssociationRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearAssociationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearValueRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ClearValueResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.CloseSessionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.CloseSessionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteClientActionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteClientActionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteServerActionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteServerActionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.FindInstancesRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.FindInstancesResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetObjectRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetObjectResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetPropertiesRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetPropertiesResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.HasInstancesRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.HasInstancesResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OidForServiceRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OidForServiceResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OpenSessionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OpenSessionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetAssociationRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetAssociationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetValueRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetValueResponse;

/**
 * previously called <tt>DummyDistribution</tt>.
 */
public class DummyServerFacade implements ServerFacade {
    public ObjectData[] allInstances(final AuthenticationSession session, final String fullName,
        final boolean includeSubclasses) {
        return null;
    }

    @Override
    public OpenSessionResponse openSession(final OpenSessionRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse authorizeUsability(final AuthorizationRequestUsability request) {
        return new AuthorizationResponse(false);
    }

    @Override
    public AuthorizationResponse authorizeVisibility(final AuthorizationRequestVisibility request) {
        return new AuthorizationResponse(false);
    }

    @Override
    public ClearAssociationResponse clearAssociation(final ClearAssociationRequest request) {
        return null;
    }

    @Override
    public ClearValueResponse clearValue(final ClearValueRequest request) {
        return null;
    }

    @Override
    public CloseSessionResponse closeSession(final CloseSessionRequest request) {
        return null;
    }

    @Override
    public ExecuteServerActionResponse executeServerAction(final ExecuteServerActionRequest request) {
        return null;
    }

    @Override
    public ExecuteClientActionResponse executeClientAction(final ExecuteClientActionRequest request) {
        return null;
    }

    @Override
    public FindInstancesResponse findInstances(final FindInstancesRequest request) {
        return null;
    }

    @Override
    public HasInstancesResponse hasInstances(final HasInstancesRequest request) {
        return null;
    }

    @Override
    public GetObjectResponse getObject(final GetObjectRequest request) {
        return null;
    }

    @Override
    public GetPropertiesResponse getProperties(final GetPropertiesRequest request) {
        return null;
    }

    @Override
    public OidForServiceResponse oidForService(final OidForServiceRequest request) {
        return null;
    }

    @Override
    public ResolveFieldResponse resolveField(final ResolveFieldRequest request) {
        return null;
    }

    @Override
    public ResolveObjectResponse resolveImmediately(final ResolveObjectRequest request) {
        return null;
    }

    @Override
    public SetAssociationResponse setAssociation(final SetAssociationRequest request) {
        return null;
    }

    @Override
    public SetValueResponse setValue(final SetValueRequest request) {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }
}
