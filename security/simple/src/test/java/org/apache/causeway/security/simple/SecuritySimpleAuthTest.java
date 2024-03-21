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
package org.apache.causeway.security.simple;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.security.simple.authentication.SimpleAuthenticator;
import org.apache.causeway.security.simple.realm.SimpleRealm;

import lombok.RequiredArgsConstructor;

class SecuritySimpleAuthTest {

    private PasswordEncoder passEncoder = new BCryptPasswordEncoder();

    private SimpleRealm realm = new SimpleRealm()
                //roles
                .addRoleWithReadAndChange("hello_role", id->id.getFullIdentityString().contains("HelloWorldObject"))
                .addRoleWithReadAndChange("admin_role", id->true)
                .addRoleWithReadAndChange("default_role", id->id.getFullIdentityString().startsWith("causeway.applib")
                        || id.getFullIdentityString().startsWith("causeway.security"))
                .addRoleWithReadAndChange("fixtures_role", id->id.getFullIdentityString().startsWith("causeway.testing.fixtures"))
                .addRoleWithReadOnly("reader_role", id->true)
                //users
                .addUser("sven", passEncoder.encode("pass0"), List.of("admin_role"))
                .addUser("dick", passEncoder.encode("pass1"), List.of("hello_role", "default_role"))
                .addUser("bob", passEncoder.encode("pass2"), List.of("hello_role", "default_role", "fixtures_role"))
                .addUser("joe", passEncoder.encode("pass3"), List.of("reader_role"));

    private SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator(realm, passEncoder);

    @RequiredArgsConstructor
    enum Scenario {
        SVEN,
        DICK,
        BOB,
        JOE;
        String userName() { return name().toLowerCase(); }
        String plainPass() { return "pass"+ordinal(); }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void authentication(final Scenario scenario) {
        // when user is null, then invalid
        assertInvalid(new AuthenticationRequestPassword(null, "123xyz"));
        // when plain pass is null, then invalid
        assertInvalid(new AuthenticationRequestPassword(scenario.userName(), null));
        // when plain pass is empty, then invalid
        assertInvalid(new AuthenticationRequestPassword(scenario.userName(), ""));
        // when plain pass is random string, then invalid
        assertInvalid(new AuthenticationRequestPassword(scenario.userName(), "123xyz"));

        // happy case
        assertValid(new AuthenticationRequestPassword(scenario.userName(), scenario.plainPass()), user->{
            assertEquals(scenario.userName(), user.getName());
        });
    }

    // -- HELPER

    void assertValid(final AuthenticationRequest request, final Consumer<UserMemento> validator) {
        var interaction = simpleAuthenticator.authenticate(request , "test");
        assertNotNull(interaction);
        validator.accept(interaction.getUser());
    }

    void assertInvalid(final AuthenticationRequest request) {
        var interaction = simpleAuthenticator.authenticate(request , "test");
        assertNull(interaction);
    }

}
