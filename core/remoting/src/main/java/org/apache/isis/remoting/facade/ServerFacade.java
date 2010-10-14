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


package org.apache.isis.remoting.facade;


import org.apache.isis.commons.components.ApplicationScopedComponent;
import org.apache.isis.remoting.exchange.AuthorizationRequestUsability;
import org.apache.isis.remoting.exchange.AuthorizationRequestVisibility;
import org.apache.isis.remoting.exchange.ClearAssociationRequest;
import org.apache.isis.remoting.exchange.ClearValueRequest;
import org.apache.isis.remoting.exchange.CloseSessionRequest;
import org.apache.isis.remoting.exchange.ExecuteClientActionRequest;
import org.apache.isis.remoting.exchange.ExecuteServerActionRequest;
import org.apache.isis.remoting.exchange.FindInstancesRequest;
import org.apache.isis.remoting.exchange.GetObjectRequest;
import org.apache.isis.remoting.exchange.GetPropertiesRequest;
import org.apache.isis.remoting.exchange.GetPropertiesResponse;
import org.apache.isis.remoting.exchange.HasInstancesRequest;
import org.apache.isis.remoting.exchange.OidForServiceRequest;
import org.apache.isis.remoting.exchange.OpenSessionRequest;
import org.apache.isis.remoting.exchange.OpenSessionResponse;
import org.apache.isis.remoting.exchange.AuthorizationResponse;
import org.apache.isis.remoting.exchange.ClearAssociationResponse;
import org.apache.isis.remoting.exchange.ClearValueResponse;
import org.apache.isis.remoting.exchange.CloseSessionResponse;
import org.apache.isis.remoting.exchange.ExecuteClientActionResponse;
import org.apache.isis.remoting.exchange.ExecuteServerActionResponse;
import org.apache.isis.remoting.exchange.FindInstancesResponse;
import org.apache.isis.remoting.exchange.GetObjectResponse;
import org.apache.isis.remoting.exchange.HasInstancesResponse;
import org.apache.isis.remoting.exchange.OidForServiceResponse;
import org.apache.isis.remoting.exchange.ResolveFieldRequest;
import org.apache.isis.remoting.exchange.ResolveFieldResponse;
import org.apache.isis.remoting.exchange.ResolveObjectRequest;
import org.apache.isis.remoting.exchange.ResolveObjectResponse;
import org.apache.isis.remoting.exchange.SetAssociationRequest;
import org.apache.isis.remoting.exchange.SetAssociationResponse;
import org.apache.isis.remoting.exchange.SetValueRequest;
import org.apache.isis.remoting.exchange.SetValueResponse;


/**
 * previously called <tt>Distribution</tt>.
 */
public interface ServerFacade extends ApplicationScopedComponent {

	///////////////////////////////////////////////////////////////////////
	// Authentication
	///////////////////////////////////////////////////////////////////////

    OpenSessionResponse openSession(OpenSessionRequest request);
    CloseSessionResponse closeSession(CloseSessionRequest request);

    
	///////////////////////////////////////////////////////////////////////
	// Authorization
	///////////////////////////////////////////////////////////////////////

    AuthorizationResponse authorizeUsability(
    		AuthorizationRequestUsability request);
    AuthorizationResponse authorizeVisibility(
    		AuthorizationRequestVisibility request);

	///////////////////////////////////////////////////////////////////////
	// Misc
	///////////////////////////////////////////////////////////////////////

    GetPropertiesResponse getProperties(GetPropertiesRequest request);
    
	///////////////////////////////////////////////////////////////////////
	// Associations (Properties and Collections)
	///////////////////////////////////////////////////////////////////////

    SetAssociationResponse setAssociation(SetAssociationRequest request);
    ClearAssociationResponse clearAssociation(ClearAssociationRequest request);

    SetValueResponse setValue(SetValueRequest request);
    ClearValueResponse clearValue(ClearValueRequest request);
    
	///////////////////////////////////////////////////////////////////////
	// Actions
	///////////////////////////////////////////////////////////////////////

    ExecuteClientActionResponse executeClientAction(
    		ExecuteClientActionRequest request);

    ExecuteServerActionResponse executeServerAction(
            ExecuteServerActionRequest request);
    
	///////////////////////////////////////////////////////////////////////
	// getObject, resolve
	///////////////////////////////////////////////////////////////////////

    OidForServiceResponse oidForService(OidForServiceRequest request);

    GetObjectResponse getObject(GetObjectRequest request);

    ResolveObjectResponse resolveImmediately(ResolveObjectRequest request);

    ResolveFieldResponse resolveField(ResolveFieldRequest request);
    
    
	///////////////////////////////////////////////////////////////////////
	// findInstances, hasInstances
	///////////////////////////////////////////////////////////////////////

    FindInstancesResponse findInstances(FindInstancesRequest request);

    HasInstancesResponse hasInstances(HasInstancesRequest request);

}
