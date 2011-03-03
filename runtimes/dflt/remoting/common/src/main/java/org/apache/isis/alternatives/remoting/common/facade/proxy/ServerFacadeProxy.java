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


package org.apache.isis.alternatives.remoting.common.facade.proxy;


import org.apache.isis.alternatives.remoting.common.IsisRemoteException;
import org.apache.isis.alternatives.remoting.common.client.ClientConnection;
import org.apache.isis.alternatives.remoting.common.exchange.AuthorizationRequestUsability;
import org.apache.isis.alternatives.remoting.common.exchange.AuthorizationRequestVisibility;
import org.apache.isis.alternatives.remoting.common.exchange.AuthorizationResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ClearAssociationRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ClearAssociationResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ClearValueRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ClearValueResponse;
import org.apache.isis.alternatives.remoting.common.exchange.CloseSessionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.CloseSessionResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteClientActionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteClientActionResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteServerActionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteServerActionResponse;
import org.apache.isis.alternatives.remoting.common.exchange.FindInstancesRequest;
import org.apache.isis.alternatives.remoting.common.exchange.FindInstancesResponse;
import org.apache.isis.alternatives.remoting.common.exchange.GetObjectRequest;
import org.apache.isis.alternatives.remoting.common.exchange.GetObjectResponse;
import org.apache.isis.alternatives.remoting.common.exchange.GetPropertiesRequest;
import org.apache.isis.alternatives.remoting.common.exchange.GetPropertiesResponse;
import org.apache.isis.alternatives.remoting.common.exchange.HasInstancesRequest;
import org.apache.isis.alternatives.remoting.common.exchange.HasInstancesResponse;
import org.apache.isis.alternatives.remoting.common.exchange.OidForServiceRequest;
import org.apache.isis.alternatives.remoting.common.exchange.OidForServiceResponse;
import org.apache.isis.alternatives.remoting.common.exchange.OpenSessionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.OpenSessionResponse;
import org.apache.isis.alternatives.remoting.common.exchange.Request;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveFieldRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveFieldResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveObjectRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveObjectResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ResponseEnvelope;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationResponse;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueResponse;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.log4j.Logger;

/**
 * previously called <tt>ClientConnection</tt>.
 */
public class ServerFacadeProxy implements ServerFacade {
    
    private static final Logger LOG = Logger.getLogger(ServerFacadeProxy.class);
    
    private final ClientConnection connection;

    ///////////////////////////////////////////////////////////////////
    // constructor
    ///////////////////////////////////////////////////////////////////

    public ServerFacadeProxy(final ClientConnection connection) {
    	this.connection = connection;
    }

    
    ///////////////////////////////////////////////////////////////////
    // init, shutdown
    ///////////////////////////////////////////////////////////////////

    public void init() {
        connection.init();
    }


    public void shutdown() {
        connection.shutdown();
    }

    ///////////////////////////////////////////////////////////////////
    // session
    ///////////////////////////////////////////////////////////////////

    public CloseSessionResponse closeSession(CloseSessionRequest request) {
        execute(request);
        return request.getResponse();
    }

    ///////////////////////////////////////////////////////////////////
    // authenticate, authorize
    ///////////////////////////////////////////////////////////////////

    public OpenSessionResponse openSession(OpenSessionRequest request) {
        execute(request);
        return request.getResponse();
    }

    public AuthorizationResponse authorizeUsability(
    		final AuthorizationRequestUsability request) {
        execute(request);
        return request.getResponse();
    }

    public AuthorizationResponse authorizeVisibility(
    		AuthorizationRequestVisibility request) {
        execute(request);
        return request.getResponse();
    }

    
    ///////////////////////////////////////////////////////////////////
    // setAssociation, setValue, clearAssociation, clearValue
    ///////////////////////////////////////////////////////////////////

    public SetAssociationResponse setAssociation(
            final SetAssociationRequest request) {
        execute(request);
        return request.getResponse();
    }

    public SetValueResponse setValue(
            SetValueRequest request) {
        execute(request);
        return request.getResponse();
    }

    public ClearAssociationResponse clearAssociation(
            ClearAssociationRequest request) {
        execute(request);
        return request.getResponse();
    }

    public ClearValueResponse clearValue(
    		ClearValueRequest request) {
        execute(request);
        return request.getResponse();
    }

    ///////////////////////////////////////////////////////////////////
    // executeServerAction, executeClientAction
    ///////////////////////////////////////////////////////////////////

    public ExecuteServerActionResponse executeServerAction(
            ExecuteServerActionRequest request) {
        execute(request);
        return request.getResponse();
    }

    public ExecuteClientActionResponse executeClientAction(
    		ExecuteClientActionRequest request) {
        execute(request);
        return request.getResponse();
    }

    ///////////////////////////////////////////////////////////////////
    // getObject, resolveXxx
    ///////////////////////////////////////////////////////////////////

    public GetObjectResponse getObject(GetObjectRequest request) {
        execute(request);
        return request.getResponse();
    }

    public ResolveObjectResponse resolveImmediately(
    		ResolveObjectRequest request) {
        execute(request);
        return request.getResponse();
    }

    public ResolveFieldResponse resolveField(ResolveFieldRequest request) {
        execute(request);
        return request.getResponse();
    }
    
    ///////////////////////////////////////////////////////////////////
    // findInstances, hasInstances
    ///////////////////////////////////////////////////////////////////

    public FindInstancesResponse findInstances(FindInstancesRequest request) {
        execute(request);
        return request.getResponse();
    }

    public HasInstancesResponse hasInstances(HasInstancesRequest request) {
        execute(request);
        return request.getResponse();
    }

    ///////////////////////////////////////////////////////////////////
    // services
    ///////////////////////////////////////////////////////////////////

    public OidForServiceResponse oidForService(
    		OidForServiceRequest request) {
        execute(request);
        return request.getResponse();
    }

    ///////////////////////////////////////////////////////////////////
    // getProperties
    ///////////////////////////////////////////////////////////////////

    public GetPropertiesResponse getProperties(GetPropertiesRequest request) {
        execute(request);
        return request.getResponse();
    }


    ///////////////////////////////////////////////////////////////////
    // Helpers: execute
    ///////////////////////////////////////////////////////////////////

    private void execute(final Request request) {
        synchronized (connection) {
            final ResponseEnvelope response = connection.executeRemotely(request);
            if (request.getId() != response.getId()) {
                throw new IsisRemoteException("Response out of sequence with respect to the request: " + request.getId()
                        + " & " + response.getId() + " respectively");
            }
            if (LOG.isDebugEnabled()) {
            	LOG.debug("response " + response);
            }
            request.setResponse(response.getObject());
        }
    }

    ///////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    ///////////////////////////////////////////////////////////////////
    
}
