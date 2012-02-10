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

package org.apache.isis.runtimes.dflt.remoting.common.facade.proxy;

import org.apache.log4j.Logger;

import org.apache.isis.runtimes.dflt.remoting.common.IsisRemoteException;
import org.apache.isis.runtimes.dflt.remoting.common.client.ClientConnection;
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
import org.apache.isis.runtimes.dflt.remoting.common.exchange.Request;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResponseEnvelope;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetAssociationRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetAssociationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetValueRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.SetValueResponse;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;

/**
 * previously called <tt>ClientConnection</tt>.
 */
public class ServerFacadeProxy implements ServerFacade {

    private static final Logger LOG = Logger.getLogger(ServerFacadeProxy.class);

    private final ClientConnection connection;

    // /////////////////////////////////////////////////////////////////
    // constructor
    // /////////////////////////////////////////////////////////////////

    public ServerFacadeProxy(final ClientConnection connection) {
        this.connection = connection;
    }

    // /////////////////////////////////////////////////////////////////
    // init, shutdown
    // /////////////////////////////////////////////////////////////////

    @Override
    public void init() {
        connection.init();
    }

    @Override
    public void shutdown() {
        connection.shutdown();
    }

    // /////////////////////////////////////////////////////////////////
    // session
    // /////////////////////////////////////////////////////////////////

    @Override
    public CloseSessionResponse closeSession(final CloseSessionRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // authenticate, authorize
    // /////////////////////////////////////////////////////////////////

    @Override
    public OpenSessionResponse openSession(final OpenSessionRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public AuthorizationResponse authorizeUsability(final AuthorizationRequestUsability request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public AuthorizationResponse authorizeVisibility(final AuthorizationRequestVisibility request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // setAssociation, setValue, clearAssociation, clearValue
    // /////////////////////////////////////////////////////////////////

    @Override
    public SetAssociationResponse setAssociation(final SetAssociationRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public SetValueResponse setValue(final SetValueRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public ClearAssociationResponse clearAssociation(final ClearAssociationRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public ClearValueResponse clearValue(final ClearValueRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // executeServerAction, executeClientAction
    // /////////////////////////////////////////////////////////////////

    @Override
    public ExecuteServerActionResponse executeServerAction(final ExecuteServerActionRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public ExecuteClientActionResponse executeClientAction(final ExecuteClientActionRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // getObject, resolveXxx
    // /////////////////////////////////////////////////////////////////

    @Override
    public GetObjectResponse getObject(final GetObjectRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public ResolveObjectResponse resolveImmediately(final ResolveObjectRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public ResolveFieldResponse resolveField(final ResolveFieldRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // findInstances, hasInstances
    // /////////////////////////////////////////////////////////////////

    @Override
    public FindInstancesResponse findInstances(final FindInstancesRequest request) {
        execute(request);
        return request.getResponse();
    }

    @Override
    public HasInstancesResponse hasInstances(final HasInstancesRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // services
    // /////////////////////////////////////////////////////////////////

    @Override
    public OidForServiceResponse oidForService(final OidForServiceRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // getProperties
    // /////////////////////////////////////////////////////////////////

    @Override
    public GetPropertiesResponse getProperties(final GetPropertiesRequest request) {
        execute(request);
        return request.getResponse();
    }

    // /////////////////////////////////////////////////////////////////
    // Helpers: execute
    // /////////////////////////////////////////////////////////////////

    private void execute(final Request request) {
        synchronized (connection) {
            final ResponseEnvelope response = connection.executeRemotely(request);
            if (request.getId() != response.getId()) {
                throw new IsisRemoteException("Response out of sequence with respect to the request: " + request.getId() + " & " + response.getId() + " respectively");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("response " + response);
            }
            request.setResponse(response.getObject());
        }
    }

    // /////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////////////

}
