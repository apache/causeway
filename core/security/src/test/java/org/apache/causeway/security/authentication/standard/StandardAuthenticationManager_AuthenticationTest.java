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
package org.apache.causeway.security.authentication.standard;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGeneratorDefault;
import org.apache.causeway.security.AuthenticatorsForTesting;

class StandardAuthenticationManager_AuthenticationTest {

    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() throws Exception {

        authenticationManager = new AuthenticationManager(
                Collections.singletonList(AuthenticatorsForTesting.authenticatorValidForFoo()),
                new InteractionService_forTesting(),
                new RandomCodeGeneratorDefault(),
                Optional.empty(),
                Collections.emptyList());
    }

    @Test
    public void newlyCreatedAuthenticationShouldBeValid() throws Exception {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("foo", "bar");
        final InteractionContext authentication = authenticationManager.authenticate(request);

        assertThat(authenticationManager.isSessionValid(authentication), is(true));
    }

    @Test
    public void newlyCreatedAuthentication_whenUnauthorizedUser_shouldBeRejected() throws Exception {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("me", "pass");
        final InteractionContext authentication = authenticationManager.authenticate(request);

        assertThat(authenticationManager.isSessionValid(authentication), is(false));
    }

}
