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
package org.apache.causeway.core.runtimeservices.command;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.HiddenException;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Core.RuntimeServices.CommandExecutorService.InteractionAdvisorPolicy;
import org.apache.causeway.core.metamodel.consent.Allow;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.Veto;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommandExecutorInteractionAdvisorTest {

    @Test
    void noCheckSkipsActionAdvisors() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");

        CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.NO_CHECK,
                action,
                target,
                InteractionHead.regular(target),
                Can.empty());

        verifyNoInteractions(action);
    }

    @Test
    void checkInvokesActionAdvisorsInOrder() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");
        var head = InteractionHead.regular(target);
        var arguments = Can.of(managedObject("argument"));
        allowAction(action, target, head, arguments);

        CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, action, target, head, arguments);

        InOrder ordered = inOrder(action);
        ordered.verify(action).isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(action).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(action).isArgumentSetValid(head, arguments, InteractionInitiatedBy.FRAMEWORK);
    }

    @Test
    void checkStopsAfterActionVisibilityVeto() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");
        when(action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE))
                .thenReturn(veto("hidden"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK,
                action,
                target,
                InteractionHead.regular(target),
                Can.empty()))
                .isInstanceOf(HiddenException.class)
                .hasMessageContaining("hidden");

        verify(action, never()).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
    }

    @Test
    void checkStopsAfterActionUsabilityVeto() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");
        var head = InteractionHead.regular(target);
        when(action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(action.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("disabled"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, action, target, head, Can.empty()))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("disabled");

        verify(action, never()).isArgumentSetValid(head, Can.empty(), InteractionInitiatedBy.FRAMEWORK);
    }

    @Test
    void checkRejectsInvalidActionArguments() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");
        var head = InteractionHead.regular(target);
        var arguments = Can.of(managedObject("argument"));
        when(action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(action.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(action.isArgumentSetValid(head, arguments, InteractionInitiatedBy.FRAMEWORK))
                .thenReturn(veto("invalid"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, action, target, head, arguments))
                .isInstanceOf(InvalidException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void checkButIgnoreInvokesEveryActionAdvisorAfterVetoes() {
        var action = mock(ObjectAction.class);
        var target = managedObject("target");
        var head = InteractionHead.regular(target);
        var arguments = Can.of(managedObject("argument"));
        when(action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("hidden"));
        when(action.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("disabled"));
        when(action.isArgumentSetValid(head, arguments, InteractionInitiatedBy.FRAMEWORK))
                .thenReturn(veto("invalid"));

        CommandExecutorServiceDefault.applyActionAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK_BUT_IGNORE, action, target, head, arguments);

        InOrder ordered = inOrder(action);
        ordered.verify(action).isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(action).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(action).isArgumentSetValid(head, arguments, InteractionInitiatedBy.FRAMEWORK);
    }

    @Test
    void noCheckSkipsPropertyAdvisors() {
        var property = mock(OneToOneAssociation.class);

        CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.NO_CHECK,
                property,
                managedObject("target"),
                managedObject("value"));

        verifyNoInteractions(property);
    }

    @Test
    void checkInvokesPropertyAdvisorsInOrder() {
        var property = mock(OneToOneAssociation.class);
        var target = managedObject("target");
        var proposed = managedObject("value");
        allowProperty(property, target, proposed);

        CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, property, target, proposed);

        InOrder ordered = inOrder(property);
        ordered.verify(property).isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(property).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(property).isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK);
    }

    @Test
    void checkStopsAfterPropertyVisibilityVeto() {
        var property = mock(OneToOneAssociation.class);
        var target = managedObject("target");
        when(property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE))
                .thenReturn(veto("hidden"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, property, target, managedObject("value")))
                .isInstanceOf(HiddenException.class)
                .hasMessageContaining("hidden");

        verify(property, never()).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
    }

    @Test
    void checkStopsAfterPropertyUsabilityVeto() {
        var property = mock(OneToOneAssociation.class);
        var target = managedObject("target");
        var proposed = managedObject("value");
        when(property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(property.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("disabled"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, property, target, proposed))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("disabled");

        verify(property, never()).isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK);
    }

    @Test
    void checkRejectsInvalidPropertyValue() {
        var property = mock(OneToOneAssociation.class);
        var target = managedObject("target");
        var proposed = managedObject("value");
        when(property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(property.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(property.isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK))
                .thenReturn(veto("invalid"));

        assertThatThrownBy(() -> CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK, property, target, proposed))
                .isInstanceOf(InvalidException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void checkButIgnoreInvokesEveryPropertyAdvisorAfterVetoes() {
        var property = mock(OneToOneAssociation.class);
        var target = managedObject("target");
        var proposed = managedObject("value");
        when(property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("hidden"));
        when(property.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(veto("disabled"));
        when(property.isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK))
                .thenReturn(veto("invalid"));

        CommandExecutorServiceDefault.applyPropertyAdvisorPolicy(
                InteractionAdvisorPolicy.CHECK_BUT_IGNORE, property, target, proposed);

        InOrder ordered = inOrder(property);
        ordered.verify(property).isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(property).isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE);
        ordered.verify(property).isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK);
    }

    private static void allowAction(
            final ObjectAction action,
            final ManagedObject target,
            final InteractionHead head,
            final Can<ManagedObject> arguments) {
        when(action.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(action.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(action.isArgumentSetValid(head, arguments, InteractionInitiatedBy.FRAMEWORK)).thenReturn(allow());
    }

    private static void allowProperty(
            final OneToOneAssociation property,
            final ManagedObject target,
            final ManagedObject proposed) {
        when(property.isVisible(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(property.isUsable(target, InteractionInitiatedBy.FRAMEWORK, Where.ANYWHERE)).thenReturn(allow());
        when(property.isAssociationValid(target, proposed, InteractionInitiatedBy.FRAMEWORK)).thenReturn(allow());
    }

    private static Consent allow() {
        return Allow.DEFAULT;
    }

    private static Consent veto(final String reason) {
        return new Veto(reason);
    }

    private static ManagedObject managedObject(final Object pojo) {
        var specification = mock(ObjectSpecification.class);
        var specificationLoader = mock(SpecificationLoader.class);
        when(specification.isValue()).thenReturn(true);
        when(specification.getBeanSort()).thenReturn(BeanSort.VALUE);
        when(specification.getSpecificationLoader()).thenReturn(specificationLoader);
        when(specificationLoader.specForType(pojo.getClass())).thenReturn(Optional.of(specification));
        return ManagedObject.value(specification, pojo);
    }
}
