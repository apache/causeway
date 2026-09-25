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
package org.apache.causeway.applib.services.iactn;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.ActionInvocation.RuleChecking;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;

class InteractionProviderTest {

    private static class Customer {}
    private static class Customer_updateName {}

    private final Object target = new String("target");
    private final Identifier actionIdentifier = Identifier.actionIdentifier(
            LogicalType.fqcn(Customer.class), "invoice", String.class);

    @Test
    void currentActionInvocation_whenThereIsNoInteraction() {
        var provider = providerFor(Optional.empty());

        assertTrue(provider.currentActionInvocation().isEmpty());
        assertFalse(provider.isCurrentActionInvocation(target, actionIdentifier));
        assertFalse(provider.isCurrentActionInvocation(target, "invoice"));
    }

    @Test
    void currentActionInvocation_whenCurrentExecutionIsNotAnAction() {
        var interaction = interactionWith(new PropertyEdit(
                null,
                Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "name"),
                target,
                "new name"));
        var provider = providerFor(Optional.of(interaction));

        assertTrue(provider.currentActionInvocation().isEmpty());
    }

    @Test
    void currentActionInvocation_whenCurrentExecutionIsAnAction() {
        var actionInvocation = new ActionInvocation(
                null, actionIdentifier, target, Collections.singletonList("arg"), RuleChecking.CHECKED);
        var provider = providerFor(Optional.of(interactionWith(actionInvocation)));

        assertSame(actionInvocation, provider.currentActionInvocation().orElseThrow());
        assertSame(RuleChecking.CHECKED, actionInvocation.getRuleChecking());
        assertTrue(provider.isCurrentActionInvocation(target, actionIdentifier));
        assertTrue(provider.isCurrentActionInvocation(target, "invoice"));
        assertTrue(provider.isCurrentActionInvocation(target, actionIdentifier, RuleChecking.CHECKED));
        assertTrue(provider.isCurrentActionInvocation(target, "invoice", RuleChecking.CHECKED));
        assertFalse(provider.isCurrentActionInvocation(target, actionIdentifier, RuleChecking.SKIPPED));
        assertFalse(provider.isCurrentActionInvocation(target, "invoice", RuleChecking.SKIPPED));
    }

    @Test
    void currentActionInvocation_matchesInvokedMixinMethodRatherThanDomainFacingIdentity() {
        final Identifier domainFacingIdentifier = Identifier.actionIdentifier(
                LogicalType.fqcn(Customer.class), "updateName", String.class);
        final Identifier invokedMemberIdentifier = Identifier.actionIdentifier(
                LogicalType.fqcn(Customer_updateName.class), "act", String.class);
        var actionInvocation = new ActionInvocation(
                null,
                invokedMemberIdentifier,
                domainFacingIdentifier,
                target,
                Collections.singletonList("arg"),
                RuleChecking.CHECKED);
        var provider = providerFor(Optional.of(interactionWith(actionInvocation)));

        assertSame(invokedMemberIdentifier, actionInvocation.getLogicalMemberIdentifier());
        assertSame(domainFacingIdentifier,
                actionInvocation.getDomainFacingLogicalMemberIdentifier());
        assertTrue(provider.isCurrentActionInvocation(target, invokedMemberIdentifier));
        assertTrue(provider.isCurrentActionInvocation(target, "act"));
        assertFalse(provider.isCurrentActionInvocation(target, domainFacingIdentifier));
        assertFalse(provider.isCurrentActionInvocation(target, "updateName"));
    }

    @Test
    void currentActionInvocation_compatibilityConstructorHasUnknownRuleChecking() {
        var actionInvocation = new ActionInvocation(
                null, actionIdentifier, target, Collections.emptyList());

        assertSame(RuleChecking.UNKNOWN, actionInvocation.getRuleChecking());
    }

    @Test
    void isCurrentActionInvocation_requiresMatchingIdentifier() {
        var actionInvocation = new ActionInvocation(
                null, actionIdentifier, target, Collections.emptyList(), RuleChecking.SKIPPED);
        var provider = providerFor(Optional.of(interactionWith(actionInvocation)));
        var otherIdentifier = Identifier.actionIdentifier(
                LogicalType.fqcn(Customer.class), "collect", String.class);

        assertFalse(provider.isCurrentActionInvocation(target, otherIdentifier));
        assertFalse(provider.isCurrentActionInvocation(target, "collect"));
        assertTrue(provider.isCurrentActionInvocation(target, actionIdentifier, RuleChecking.SKIPPED));
        assertTrue(provider.isCurrentActionInvocation(target, "invoice", RuleChecking.SKIPPED));
    }

    @Test
    void isCurrentActionInvocation_comparesTargetByIdentity() {
        var equalButNotIdenticalTarget = new String("target");
        var actionInvocation = new ActionInvocation(
                null, actionIdentifier, target, Collections.emptyList(), RuleChecking.CHECKED);
        var provider = providerFor(Optional.of(interactionWith(actionInvocation)));

        assertTrue(target.equals(equalButNotIdenticalTarget));
        assertFalse(provider.isCurrentActionInvocation(equalButNotIdenticalTarget, actionIdentifier));
        assertFalse(provider.isCurrentActionInvocation(equalButNotIdenticalTarget, "invoice"));
    }

    private static Interaction interactionWith(final Execution<?, ?> currentExecution) {
        return (Interaction) Proxy.newProxyInstance(
                Interaction.class.getClassLoader(),
                new Class<?>[] { Interaction.class },
                (proxy, method, args) -> "getCurrentExecution".equals(method.getName())
                        ? currentExecution
                        : defaultValue(method.getReturnType()));
    }

    private static InteractionProvider providerFor(final Optional<Interaction> interaction) {
        return new InteractionProvider() {
            @Override public boolean isInInteraction() { return interaction.isPresent(); }
            @Override public Optional<Interaction> currentInteraction() { return interaction; }
            @Override public Optional<InteractionContext> currentInteractionContext() { return Optional.empty(); }
            @Override public Optional<UUID> getInteractionId() { return Optional.empty(); }
            @Override public int getInteractionLayerCount() { return interaction.isPresent() ? 1 : 0; }
        };
    }

    private static Object defaultValue(final Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return 0;
    }
}
