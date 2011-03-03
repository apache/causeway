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


package org.apache.isis.alternatives.remoting.common.facade;


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
import org.apache.isis.alternatives.remoting.common.exchange.ResolveFieldRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveFieldResponse;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveObjectRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ResolveObjectResponse;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetAssociationResponse;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueRequest;
import org.apache.isis.alternatives.remoting.common.exchange.SetValueResponse;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;


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
