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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

class ParentedCollectionSelectorActionFactoryTest {

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class Lease {
        @Getter
        private final List<LeaseItem> items = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class LeaseItem {
        @Getter
        private final String name;
        @Getter
        private final Integer sequence;
        @Getter
        private final Lease otherLease;
        @Getter
        private final List<LeaseTerm> terms = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class LeaseTerm {
        @Getter
        private final String description;
    }

    private MetaModelContext_forTesting mmc;
    private ObjectSpecification leaseSpec;
    private ObjectAction selectorAction;

    @BeforeEach
    void setUp() {
        mmc = newMetamodelContext();
        mmc.getConfiguration().getExtensions().getCommandLog().setParentedCollectionSelectorActionsEnabled(true);
        leaseSpec = mmc.getSpecificationLoader().loadSpecification(Lease.class);
        selectorAction = leaseSpec.getAction(ParentedCollectionSelectorActionFactory.ACTION_ID_PREFIX + "items").orElseThrow();
    }

    @Test
    void does_not_synthesize_selector_action_unless_enabled() {
        val disabledMmc = newMetamodelContext();
        val disabledLeaseSpec = disabledMmc.getSpecificationLoader().loadSpecification(Lease.class);

        assertThat(disabledLeaseSpec.getAction(ParentedCollectionSelectorActionFactory.ACTION_ID_PREFIX + "items").isPresent(), is(false));
    }

    @Test
    void synthesizes_selector_action_with_marker_and_safe_semantics_when_enabled() {
        assertThat(selectorAction.getId(), is(ParentedCollectionSelectorActionFactory.ACTION_ID_PREFIX + "items"));
        assertThat(selectorAction.getFacet(ParentedCollectionSelectorFacet.class), instanceOf(ParentedCollectionSelectorFacetDefault.class));
        assertThat(selectorAction.getSemantics(), is(SemanticsOf.SAFE));
        assertThat(selectorAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(false));
    }

    @Test
    void associates_selector_action_with_parented_collection() {
        val layoutGroupFacet = selectorAction.getFacet(LayoutGroupFacet.class);

        assertThat(layoutGroupFacet, instanceOf(LayoutGroupFacetForParentedCollectionSelector.class));
        assertThat(layoutGroupFacet.getGroupId(), is("items"));
        assertThat(layoutGroupFacet.getGroupName(), is("Items"));
    }

    @Test
    void names_selector_action_select_while_preserving_deterministic_id() {
        assertThat(selectorAction.getId(), is(ParentedCollectionSelectorActionFactory.ACTION_ID_PREFIX + "items"));
        assertThat(selectorAction.getCanonicalFriendlyName(), is("Select"));
    }

    @Test
    void participates_in_safe_action_command_publishing_when_enabled() {
        mmc.getConfiguration().getExtensions().getCommandLog().setSafeActionCommandPublishing(true);

        assertThat(selectorAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(true));
    }

    @Test
    void exposes_mandatory_parent_parameter_and_optional_scalar_child_parameters() {
        val parameters = selectorAction.getParameters();
        assertThat(parameters.getElseFail(0).getId(), is("lease"));
        assertThat(parameters.getElseFail(0).getElementType().getCorrespondingClass(), is(Lease.class));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("name")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("sequence")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("otherLease")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("terms")), is(false));
    }

    @Test
    void defaults_parent_parameter_to_action_target() {
        val lease = new Lease();
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        val pendingArgs = Mockito.mock(ParameterNegotiationModel.class);
        Mockito.when(pendingArgs.getActionTarget()).thenReturn(leaseAdapter);

        val parentParameter = selectorAction.getParameters().getElseFail(0);
        val defaultsFacet = parentParameter.getFacet(ActionParameterDefaultsFacet.class);

        assertThat(defaultsFacet, instanceOf(ActionParameterDefaultsFacetForParentedCollectionSelectorParent.class));
        assertThat(parentParameter.hasDefaults(), is(true));
        assertThat(parentParameter.getDefault(pendingArgs), is(leaseAdapter));
    }

    @Test
    void disables_parent_parameter_but_not_scalar_filter_parameters() {
        val lease = new Lease();
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        val pendingArgs = Can.of(
                leaseAdapter,
                ManagedObject.empty(mmc.getSpecificationLoader().loadSpecification(String.class)),
                ManagedObject.empty(mmc.getSpecificationLoader().loadSpecification(Integer.class)));
        val parameters = selectorAction.getParameters();
        val parentParameter = parameters.getElseFail(0);
        val scalarParameter = parameters.getElseFail(1);

        assertThat(parentParameter.getFacet(DisabledFacet.class), instanceOf(DisabledFacetForParentedCollectionSelectorParent.class));
        assertThat(parentParameter.isUsable(
                selectorAction.interactionHead(leaseAdapter),
                pendingArgs,
                InteractionInitiatedBy.USER).isVetoed(), is(true));
        assertThat(scalarParameter.getFacet(DisabledFacet.class), is((DisabledFacet) null));
        assertThat(scalarParameter.isUsable(
                selectorAction.interactionHead(leaseAdapter),
                pendingArgs,
                InteractionInitiatedBy.USER).isAllowed(), is(true));
    }

    @Test
    void invokes_selector_action_returning_single_matching_child() {
        val lease = new Lease();
        val matchingItem = new LeaseItem("first", 1, null);
        lease.getItems().add(matchingItem);
        lease.getItems().add(new LeaseItem("second", 2, null));

        val result = invoke(lease, "first", null);

        assertThat(result.getPojo(), is(matchingItem));
    }

    @Test
    void selector_action_fails_when_no_child_matches() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 1, null));

        val ex = assertThrows(IllegalArgumentException.class, () -> invoke(lease, "missing", null));

        assertThat(ex.getMessage(), containsString("no element"));
    }

    @Test
    void selector_action_fails_when_multiple_children_match() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("same", 1, null));
        lease.getItems().add(new LeaseItem("same", 2, null));

        val ex = assertThrows(IllegalArgumentException.class, () -> invoke(lease, "same", null));

        assertThat(ex.getMessage(), containsString("multiple elements"));
    }

    private MetaModelContext_forTesting newMetamodelContext() {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .build();
    }

    private ManagedObject invoke(
            final Lease lease,
            final String name,
            final Integer sequence) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        val actionInvocationFacet = selectorAction.getFacet(ActionInvocationFacet.class);
        return actionInvocationFacet.invoke(
                selectorAction,
                selectorAction.interactionHead(leaseAdapter),
                Can.of(
                        leaseAdapter,
                        mmc.getObjectManager().adapt(name),
                        mmc.getObjectManager().adapt(sequence)),
                InteractionInitiatedBy.USER);
    }

}
