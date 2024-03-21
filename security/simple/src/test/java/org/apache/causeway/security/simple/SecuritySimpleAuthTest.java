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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.security.simple.authentication.SimpleAuthenticator;
import org.apache.causeway.security.simple.authorization.SimpleAuthorizor;
import org.apache.causeway.security.simple.realm.SimpleRealm;
import org.apache.causeway.security.simple.realm.SimpleRealm.Grant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

class SecuritySimpleAuthTest {

    private PasswordEncoder passEncoder = new BCryptPasswordEncoder();

    private SimpleRealm realm = new SimpleRealm()
                //roles
                .addRole("admin_role", id->Grant.CHANGE)
                .addRole("order_role", id->
                    id.getFullIdentityString().contains("Order")
                        ? Grant.CHANGE
                        : Grant.NONE)
                .addRole("customer_role", id->
                    id.getFullIdentityString().contains("Customer")
                        ? Grant.CHANGE
                        : Grant.NONE)
                .addRole("reader_role", id->
                    id.getFullIdentityString().contains("TopSecret")
                        ? Grant.NONE
                        : Grant.READ)
                //users
                .addUser("sven", passEncoder.encode("pass0"), List.of("admin_role"))
                .addUser("dick", passEncoder.encode("pass1"), List.of("reader_role", "order_role"))
                .addUser("bob", passEncoder.encode("pass2"), List.of("reader_role", "customer_role"))
                .addUser("joe", passEncoder.encode("pass3"), List.of("reader_role"));

    private SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator(realm, passEncoder);
    private SimpleAuthorizor simpleAuthorizor = new SimpleAuthorizor(realm);

    // -- SCENARIOS

    static class Order {
        String name;
    }
    static class Customer {
        String name;
    }
    static class TopSecret {
        String name;
    }

    @RequiredArgsConstructor @Getter @Accessors(fluent=true)
    enum Scenario {
        SVEN(
                Can.of(ord(), cus(), top()), // read grant
                Can.of(ord(), cus(), top()), // change grant
                Can.empty(), // read deny
                Can.empty()), // change deny
        DICK(
                Can.of(ord(), cus()), // read grant
                Can.of(ord()), // change grant
                Can.of(top()), // read deny
                Can.of(cus(), top())), // change deny
        BOB(
                Can.of(ord(), cus()), // read grant
                Can.of(cus()), // change grant
                Can.of(top()), // read deny
                Can.of(ord(), top())), // change deny
        JOE(
                Can.of(ord(), cus()), // read grant
                Can.empty(), // change grant
                Can.of(top()), // read deny
                Can.of(ord(), cus(), top())); // change deny
        String userName() { return name().toLowerCase(); }
        String plainPass() { return "pass"+ordinal(); }
        final Can<Identifier> expectedReadGranted;
        final Can<Identifier> expectedChangeGranted;
        final Can<Identifier> expectedReadDenied;
        final Can<Identifier> expectedChangeDenied;
        static Identifier ord() {
            return Identifier.propertyIdentifier(LogicalType.fqcn(Order.class), "name");
        }
        static Identifier cus() {
            return Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "name");
        }
        static Identifier top() {
            return Identifier.propertyIdentifier(LogicalType.fqcn(TopSecret.class), "name");
        }
    }

    // -- TESTS

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
        assertValid(new AuthenticationRequestPassword(scenario.userName(), scenario.plainPass()), interactionContext->{
            assertEquals(scenario.userName(), interactionContext.getUser().getName());
        });
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void authorization(final Scenario scenario) {
        assertValid(new AuthenticationRequestPassword(scenario.userName(), scenario.plainPass()), interactionContext->{
            scenario.expectedReadGranted().forEach(grant->
                assertTrue(simpleAuthorizor.isVisible(interactionContext, grant)));
            scenario.expectedChangeGranted().forEach(grant->
                assertTrue(simpleAuthorizor.isUsable(interactionContext, grant)));
            scenario.expectedReadDenied().forEach(veto->
                assertFalse(simpleAuthorizor.isVisible(interactionContext, veto)));
            scenario.expectedChangeDenied().forEach(veto->
                assertFalse(simpleAuthorizor.isUsable(interactionContext, veto)));
        });
    }

    // -- HELPER

    void assertValid(final AuthenticationRequest request, final Consumer<InteractionContext> validator) {
        var interactionContext = simpleAuthenticator.authenticate(request , "test");
        assertNotNull(interactionContext);
        validator.accept(interactionContext);
    }

    void assertInvalid(final AuthenticationRequest request) {
        var interaction = simpleAuthenticator.authenticate(request , "test");
        assertNull(interaction);
    }

}
