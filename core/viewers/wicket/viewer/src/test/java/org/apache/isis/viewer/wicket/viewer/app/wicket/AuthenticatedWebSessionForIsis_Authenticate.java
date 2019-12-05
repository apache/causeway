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

package org.apache.isis.viewer.wicket.viewer.app.wicket;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.security.api.authentication.AuthenticationRequest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class AuthenticatedWebSessionForIsis_Authenticate 
extends AuthenticatedWebSessionForIsis_TestAbstract {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        context.checking(new Expectations() {
            {
                oneOf(mockAuthMgr).authenticate(with(any(AuthenticationRequest.class)));
            }
        });

        super.setupWebSession();
        
        assertThat(webSession.authenticate("jsmith", "secret"), is(true));
        assertThat(webSession.getAuthenticationSession(), is(not(nullValue())));
    }

    @Test
    public void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {
        context.checking(new Expectations() {
            {
                oneOf(mockAuthMgr).authenticate(with(any(AuthenticationRequest.class)));
                will(returnValue(null));
            }
        });
        
        super.setupWebSession();
        
        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthenticationSession(), is(nullValue()));
    }

}
