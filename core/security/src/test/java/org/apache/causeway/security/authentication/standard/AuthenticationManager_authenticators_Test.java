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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.exceptions.unrecoverable.NoAuthenticatorException;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGeneratorDefault;
import org.apache.causeway.security.AuthenticatorsForTesting;

import lombok.val;

class AuthenticationManager_authenticators_Test {

    private AuthenticationManager authenticationManager;

    @Test
    public void shouldNotBeAbleToAuthenticateWithNoAuthenticators() throws Exception {

        assertThrows(NoAuthenticatorException.class, ()->{

            authenticationManager = new AuthenticationManager(
                    Collections.emptyList(),
                    new InteractionService_forTesting(),
                    new RandomCodeGeneratorDefault(),
                    Optional.empty(),
                    Collections.emptyList());
            authenticationManager.authenticate(new AuthenticationRequestPassword("foo", "bar"));

        });
    }

    @Test
    public void shouldBeAbleToUseAuthenticators() throws Exception {

        val auth = AuthenticatorsForTesting.authenticatorAllwaysValid();

        authenticationManager = new AuthenticationManager(
                Collections.singletonList(auth),
                new InteractionService_forTesting(),
                new RandomCodeGeneratorDefault(),
                Optional.empty(),
                Collections.emptyList());
        assertThat(authenticationManager.getAuthenticators().size(), is(1));
        assertThat(authenticationManager.getAuthenticators().getElseFail(0), is(sameInstance(auth)));
    }

}
