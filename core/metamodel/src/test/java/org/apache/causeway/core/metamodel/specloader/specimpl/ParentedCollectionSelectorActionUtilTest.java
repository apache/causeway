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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.core.metamodel.facets.actions.synthetic.*;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.exceptions.RecoverableException;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

class ParentedCollectionSelectorActionUtilTest {

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

    @RequiredArgsConstructor
    @org.apache.causeway.applib.annotation.Collection
    static class Lease_mixinItems {
        private final Lease mixee;

        @MemberSupport
        public List<LeaseItem> coll() {
            return mixee.getItems();
        }
    }

    private MetaModelContext_forTesting mmc;
    private ObjectSpecification leaseSpec;
    private ObjectAction selectorAction;

    @BeforeEach
    void setUp() {
        mmc = newMetamodelContext();
        mmc.getConfiguration().getExtensions().getCommandLog().setParentedCollectionSelectorActionsEnabled(true);
        leaseSpec = mmc.getSpecificationLoader().loadSpecification(Lease.class);
        selectorAction = leaseSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").orElseThrow();
    }

    @Test
    void does_not_synthesize_selector_action_unless_enabled() {
        val disabledMmc = newMetamodelContext();
        val disabledLeaseSpec = disabledMmc.getSpecificationLoader().loadSpecification(Lease.class);

        assertThat(disabledLeaseSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").isPresent(), is(false));
    }

    @Test
    void synthesizes_selector_action_with_marker_and_safe_semantics_when_enabled() {
        assertThat(selectorAction.getId(), is(ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items"));
        MatcherAssert.assertThat(selectorAction.getFacet(ParentedCollectionSelectorFacet.class), instanceOf(ParentedCollectionSelectorFacetDefault.class));
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
        assertThat(selectorAction.getId(), is(ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items"));
        assertThat(selectorAction.getCanonicalFriendlyName(), is("Select"));
    }

    @Test
    void participates_in_safe_action_command_publishing_when_enabled() {
        mmc.getConfiguration().getExtensions().getCommandLog().setSafeActionCommandPublishing(true);

        assertThat(selectorAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(true));
    }

    @Test
    void synthesizes_selector_action_for_mixed_in_collection_when_enabled() {
        val mixinMmc = newMetamodelContext(Lease_mixinItems.class);
        mixinMmc.getConfiguration().getExtensions().getCommandLog().setParentedCollectionSelectorActionsEnabled(true);
        val mixinLeaseSpec = mixinMmc.getSpecificationLoader().loadSpecification(Lease.class);

        val mixinSelectorAction = mixinLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "mixinItems").orElseThrow();
        val layoutGroupFacet = mixinSelectorAction.getFacet(LayoutGroupFacet.class);

        assertThat(mixinSelectorAction.getFacet(ParentedCollectionSelectorFacet.class),
                instanceOf(ParentedCollectionSelectorFacetDefault.class));
        assertThat(layoutGroupFacet.getGroupId(), is("mixinItems"));
        assertThat(layoutGroupFacet.getGroupName(), is("Mixin Items"));
        assertThat(mixinSelectorAction.getParameters().getElseFail(0).getElementType().getCorrespondingClass(), is(Lease.class));
        assertThat(mixinSelectorAction.getParameters().stream().anyMatch(parameter -> parameter.getId().equals("name")), is(true));
    }

    @Test
    void synthesizes_selector_action_for_mixed_in_collection_when_associations_are_loaded_first() {
        val mixinMmc = newMetamodelContext(Lease_mixinItems.class);
        mixinMmc.getConfiguration().getExtensions().getCommandLog().setParentedCollectionSelectorActionsEnabled(true);
        val mixinLeaseSpec = mixinMmc.getSpecificationLoader().loadSpecification(Lease.class);

        assertThat(mixinLeaseSpec.streamDeclaredAssociations(MixedIn.INCLUDED)
                .anyMatch(association -> association.getId().equals("mixinItems")), is(true));

        assertThat(mixinLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "mixinItems").isPresent(), is(true));
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
    void validates_and_invokes_selector_action_returning_single_matching_child() {
        val lease = new Lease();
        val matchingItem = new LeaseItem("first", 1, null);
        lease.getItems().add(matchingItem);
        lease.getItems().add(new LeaseItem("second", 2, null));

        assertThat(selectorAction.getFacet(ActionValidationFacet.class), instanceOf(ActionValidationFacetForParentedCollectionSelector.class));
        assertThat(validate(lease, "first", null).isAllowed(), is(true));

        val result = invoke(lease, "first", null);

        assertThat(result.getPojo(), is(matchingItem));
    }

    @Test
    void selector_action_validation_rejects_when_no_child_matches() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 1, null));

        val consent = validate(lease, "missing", null);

        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString("0 items match. Use parameters to match just one item."));

        val ex = assertThrows(RecoverableException.class, () -> executeWithRuleChecking(lease, "missing", null));

        assertThat(ex.getMessage(), containsString("0 items match. Use parameters to match just one item."));
    }

    @Test
    void selector_action_validation_rejects_when_multiple_children_match() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("same", 1, null));
        lease.getItems().add(new LeaseItem("same", 2, null));

        val consent = validate(lease, "same", null);

        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString("2 items match. Use parameters to match just one item."));

        val ex = assertThrows(RecoverableException.class, () -> executeWithRuleChecking(lease, "same", null));

        assertThat(ex.getMessage(), containsString("2 items match. Use parameters to match just one item."));
    }

    @Test
    void direct_invocation_still_fails_when_no_child_matches() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 1, null));

        val ex = assertThrows(RecoverableException.class, () -> invoke(lease, "missing", null));

        assertThat(ex.getMessage(), containsString("0 items match. Use parameters to match just one item."));
    }

    @Test
    void direct_invocation_still_fails_when_multiple_children_match() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("same", 1, null));
        lease.getItems().add(new LeaseItem("same", 2, null));

        val ex = assertThrows(RecoverableException.class, () -> invoke(lease, "same", null));

        assertThat(ex.getMessage(), containsString("2 items match. Use parameters to match just one item."));
    }

    private MetaModelContext_forTesting newMetamodelContext(final Class<?>... mixinTypes) {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .causewayBeanTypeRegistry(new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                        .map(mixinType -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, mixinType))))
                .build();
    }

    private Consent validate(
            final Lease lease,
            final String name,
            final Integer sequence) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        return selectorAction.isArgumentSetValid(
                selectorAction.interactionHead(leaseAdapter),
                arguments(leaseAdapter, name, sequence),
                InteractionInitiatedBy.USER);
    }

    private ManagedObject executeWithRuleChecking(
            final Lease lease,
            final String name,
            final Integer sequence) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        return selectorAction.executeWithRuleChecking(
                selectorAction.interactionHead(leaseAdapter),
                arguments(leaseAdapter, name, sequence),
                InteractionInitiatedBy.USER,
                null);
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
                arguments(leaseAdapter, name, sequence),
                InteractionInitiatedBy.USER);
    }

    private Can<ManagedObject> arguments(
            final ManagedObject leaseAdapter,
            final String name,
            final Integer sequence) {
        return Can.of(
                leaseAdapter,
                mmc.getObjectManager().adapt(name),
                mmc.getObjectManager().adapt(sequence));
    }

}
