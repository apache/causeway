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


package org.apache.isis.alternatives.remoting.common.client.authentication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.apache.isis.alternatives.remoting.common.exchange.OpenSessionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.OpenSessionResponse;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.authentication.standard.PasswordRequestAuthenticatorAbstract;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;

public final class ProxyAuthenticator extends PasswordRequestAuthenticatorAbstract {
	
	private final ServerFacade serverFacade;
	private final ObjectEncoderDecoder encoderDecoder;
	
	public ProxyAuthenticator(
			IsisConfiguration configuration, 
			ServerFacade serverFacade, ObjectEncoderDecoder encoderDecoder) {
		super(configuration);
		Ensure.ensureThatArg(serverFacade, is(not(nullValue())));
		Ensure.ensureThatArg(encoderDecoder, is(not(nullValue())));
		this.serverFacade = serverFacade;
		this.encoderDecoder = encoderDecoder;
	}

	/**
	 * Whereas the default implementation of {@link AuthenticatorAbstract#authenticate(AuthenticationRequest, String)}
	 * delegates to this method, this implementation has it the other way around.
	 */
	public boolean isValid(AuthenticationRequest request) {
		// our implementation does not use the code, so can pass in null.
		return authenticate(request, null) != null;
	}
	
	/**
	 * Delegates to the provided {@link ServerFacade} to {@link ServerFacade#authenticate(String)}.
	 *
	 * <p>
	 * Compare to the {@link AuthenticatorAbstract#authenticate(AuthenticationRequest, String) default implementation}
	 * which calls {@link #isValid(AuthenticationRequest)} and then returns
	 * a {@link SimpleSession}.  
	 */
	@Override
	public AuthenticationSession authenticate(AuthenticationRequest authRequest,
			String code) {
        final AuthenticationRequestPassword passwordRequest = (AuthenticationRequestPassword) authRequest;
        final String username = passwordRequest.getName();
        if (StringUtils.isNullOrEmpty(username)) {
            return null;
        }
        final String password = passwordRequest.getPassword();
        
		OpenSessionRequest request = new OpenSessionRequest(username, password);
		OpenSessionResponse response = serverFacade.openSession(request);
		return response.getSession();
	}
}