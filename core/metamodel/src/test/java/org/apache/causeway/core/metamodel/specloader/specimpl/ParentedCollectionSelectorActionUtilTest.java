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
import java.util.Optional;
import java.util.UUID;

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
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;

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
        private BoundedReference boundedReference;
        @Getter
        private SelectableReference choicesReference;
        @Getter
        private SelectableReference autocompleteReference;
        @Getter
        private ObjectAutocompleteReference objectAutocompleteReference;
        @Getter
        private SelectableReference unconstrainedReference;
        @Getter
        @PropertyLayout(hidden = Where.PARENTED_TABLES)
        private SelectableReference nonColumnChoicesReference;
        @Getter
        private final List<LeaseTerm> terms = new ArrayList<>();
        @Getter
        private Blob attachment;
        @Getter
        private Clob notes;
        @Getter
        @PropertyLayout(hidden = Where.PARENTED_TABLES)
        private String internalCode;
        @Getter
        private String logicalTypeName;
        @Getter
        private String id;
        @Getter
        private Long version;
        @Getter
        private String objectIdentifier;
        @Getter
        private Long datanucleusVersionLong;
        @Getter
        private java.sql.Timestamp datanucleusVersionTimestamp;

        @MemberSupport
        public List<SelectableReference> choicesChoicesReference() {
            return List.of();
        }

        @MemberSupport
        public List<SelectableReference> autoCompleteAutocompleteReference(final String search) {
            return List.of();
        }

        @MemberSupport
        public List<SelectableReference> choicesNonColumnChoicesReference() {
            return List.of();
        }
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL, bounding = Bounding.BOUNDED)
    static class BoundedReference {
        @Getter
        private final String title;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class SelectableReference {
        @Getter
        private final String title;
    }

    static class ObjectAutocompleteReferenceRepository {
        public List<ObjectAutocompleteReference> autoComplete(final String search) {
            return List.of();
        }
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL, autoCompleteRepository = ObjectAutocompleteReferenceRepository.class)
    static class ObjectAutocompleteReference {
        @Getter
        private final String title;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class LeaseTerm {
        @Getter
        private final String description;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class HomePageViewModel {
        @Getter
        private final List<EntityLeaseItem> items = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.ENTITY)
    static class EntityLeaseItem {
        @Getter
        private final String name;
        @Getter
        private final Integer sequence;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class OrderedLease {
        @Getter
        private final List<OrderedLeaseItem> items = new ArrayList<>();
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class OrderedLeaseItem {
        @Getter
        @PropertyLayout(sequence = "2")
        private String name;
        @Getter
        @PropertyLayout(sequence = "1")
        private Integer sequence;
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
        mmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
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
        assertThat(ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX, is("__causeway_select_from_"));
        assertThat(selectorAction.getId(), is("__causeway_select_from_items"));
        MatcherAssert.assertThat(selectorAction.getFacet(ParentedCollectionSelectorFacet.class), instanceOf(ParentedCollectionSelectorFacetDefault.class));
        assertThat(selectorAction.getSemantics(), is(SemanticsOf.SAFE));
        assertThat(selectorAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(true));
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
        assertThat(selectorAction.getId(), is("__causeway_select_from_items"));
        assertThat(selectorAction.getCanonicalFriendlyName(), is("Select"));
    }

    @Test
    void styles_selector_action_as_secondary_select_navigation() {
        assertSelectorActionStyling(selectorAction);
    }

    @Test
    void participates_in_safe_action_command_publishing_when_enabled() {
        mmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);

        assertThat(selectorAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(true));
    }

    @Test
    void synthesizes_selector_action_for_mixed_in_collection_when_enabled() {
        val mixinMmc = newMetamodelContext(Lease_mixinItems.class);
        mixinMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        val mixinLeaseSpec = mixinMmc.getSpecificationLoader().loadSpecification(Lease.class);

        val mixinSelectorAction = mixinLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "mixinItems").orElseThrow();
        val layoutGroupFacet = mixinSelectorAction.getFacet(LayoutGroupFacet.class);

        assertThat(mixinSelectorAction.getFacet(ParentedCollectionSelectorFacet.class),
                instanceOf(ParentedCollectionSelectorFacetDefault.class));
        assertThat(layoutGroupFacet.getGroupId(), is("mixinItems"));
        assertThat(layoutGroupFacet.getGroupName(), is("Mixin Items"));
        assertSelectorActionStyling(mixinSelectorAction);
        assertThat(mixinSelectorAction.getParameters().getElseFail(0).getElementType().getCorrespondingClass(), is(Lease.class));
        assertThat(mixinSelectorAction.getParameters().stream().anyMatch(parameter -> parameter.getId().equals("name")), is(true));
    }

    @Test
    void synthesizes_selector_action_for_mixed_in_collection_when_associations_are_loaded_first() {
        val mixinMmc = newMetamodelContext(Lease_mixinItems.class);
        mixinMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        val mixinLeaseSpec = mixinMmc.getSpecificationLoader().loadSpecification(Lease.class);

        assertThat(mixinLeaseSpec.streamDeclaredAssociations(MixedIn.INCLUDED)
                .anyMatch(association -> association.getId().equals("mixinItems")), is(true));

        assertThat(mixinLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "mixinItems").isPresent(), is(true));
    }

    @Test
    void synthesizes_selector_action_for_view_model_owned_collection_with_entity_elements() {
        val homePageSpec = mmc.getSpecificationLoader().loadSpecification(HomePageViewModel.class);
        val homePageSelectorAction = homePageSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").orElseThrow();

        assertThat(homePageSelectorAction.getId(), is("__causeway_select_from_items"));
        assertThat(homePageSelectorAction.getCanonicalFriendlyName(), is("Select"));
        assertThat(homePageSelectorAction.getFacet(ParentedCollectionSelectorFacet.class),
                instanceOf(ParentedCollectionSelectorFacetDefault.class));
        assertSelectorActionStyling(homePageSelectorAction);

        val layoutGroupFacet = homePageSelectorAction.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet.getGroupId(), is("items"));
        assertThat(layoutGroupFacet.getGroupName(), is("Items"));

        val parameters = homePageSelectorAction.getParameters();
        assertThat(parameters.getElseFail(0).getId(), is("homePageViewModel"));
        assertThat(parameters.getElseFail(0).getElementType().getCorrespondingClass(), is(HomePageViewModel.class));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("name")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("sequence")), is(true));
    }

    @Test
    void exposes_mandatory_parent_parameter_and_optional_scalar_and_selectable_reference_child_parameters() {
        val parameters = selectorAction.getParameters();
        assertThat(parameters.getElseFail(0).getId(), is("lease"));
        assertThat(parameters.getElseFail(0).getElementType().getCorrespondingClass(), is(Lease.class));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("name")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("sequence")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("boundedReference")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("choicesReference")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("autocompleteReference")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("objectAutocompleteReference")), is(true));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("internalCode")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("otherLease")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("unconstrainedReference")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("nonColumnChoicesReference")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("terms")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("attachment")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("notes")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("logicalTypeName")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("id")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("version")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("objectIdentifier")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("datanucleusVersionLong")), is(false));
        assertThat(parameters.stream().anyMatch(parameter -> parameter.getId().equals("datanucleusVersionTimestamp")), is(false));
    }

    @Test
    void orders_scalar_child_parameters_using_static_collection_column_order() {
        val orderedMmc = newMetamodelContext();
        orderedMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        val orderedLeaseSpec = orderedMmc.getSpecificationLoader().loadSpecification(OrderedLease.class);
        val orderedSelectorAction = orderedLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").orElseThrow();
        val parameters = orderedSelectorAction.getParameters();

        assertThat(parameters.getElseFail(0).getId(), is("orderedLease"));
        assertThat(parameters.getElseFail(1).getId(), is("sequence"));
        assertThat(parameters.getElseFail(2).getId(), is("name"));
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
    void disables_selector_action_when_associated_collection_is_empty() {
        val lease = new Lease();
        val leaseAdapter = mmc.getObjectManager().adapt(lease);

        val consent = selectorAction.isUsable(leaseAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);

        assertThat(selectorAction.getFacet(DisabledFacet.class), instanceOf(DisabledFacetForEmptyParentedCollectionSelector.class));
        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString(DisabledFacetForEmptyParentedCollectionSelector.REASON));
    }

    @Test
    void leaves_selector_action_enabled_when_associated_collection_is_not_empty() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 1, null));
        val leaseAdapter = mmc.getObjectManager().adapt(lease);

        assertThat(selectorAction.isUsable(leaseAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed(), is(true));
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
    void partial_string_filter_validates_and_invokes_when_it_identifies_one_child() {
        val lease = new Lease();
        val matchingItem = new LeaseItem("first child", 1, null);
        lease.getItems().add(matchingItem);
        lease.getItems().add(new LeaseItem("second child", 2, null));

        assertThat(validate(lease, "irs", null).isAllowed(), is(true));

        val result = invoke(lease, "irs", null);

        assertThat(result.getPojo(), is(matchingItem));
    }

    @Test
    void non_string_scalar_filter_still_uses_exact_equality() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 12, null));

        val consent = validate(lease, "first", 1);

        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString("0 items match. Use parameters to match just one item."));
    }

    @Test
    void reference_filter_validates_and_invokes_when_it_identifies_one_child() {
        val lease = new Lease();
        val matchingReference = new BoundedReference("same title");
        val otherReference = new BoundedReference("same title");
        val matchingItem = new LeaseItem("first", 1, null);
        matchingItem.boundedReference = matchingReference;
        val otherItem = new LeaseItem("second", 2, null);
        otherItem.boundedReference = otherReference;
        lease.getItems().add(matchingItem);
        lease.getItems().add(otherItem);

        assertThat(validate(lease, null, null, matchingReference, null, null).isAllowed(), is(true));

        val result = invoke(lease, null, null, matchingReference, null, null);

        assertThat(result.getPojo(), is(matchingItem));
    }

    @Test
    void reference_filter_uses_exact_reference_equality_not_title_or_partial_string_matching() {
        val lease = new Lease();
        val storedReference = new BoundedReference("same title");
        val suppliedDifferentReferenceWithSameTitle = new BoundedReference("same title");
        val matchingItem = new LeaseItem("first", 1, null);
        matchingItem.boundedReference = storedReference;
        lease.getItems().add(matchingItem);

        val consent = validate(lease, null, null, suppliedDifferentReferenceWithSameTitle, null, null);

        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString("0 items match. Use parameters to match just one item."));
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
    void partial_string_filter_is_ambiguous_when_it_matches_multiple_children() {
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first child", 1, null));
        lease.getItems().add(new LeaseItem("second child", 2, null));

        val consent = validate(lease, "child", null);

        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString("2 items match. Use parameters to match just one item."));

        val ex = assertThrows(RecoverableException.class, () -> executeWithRuleChecking(lease, "child", null));

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

    @Test
    void selector_invocation_prepares_command_for_publishing_when_safe_action_publishing_enabled() {
        val commandPublisher = Mockito.mock(CommandPublisher.class);
        val interactionProvider = Mockito.mock(InteractionProvider.class);
        val interaction = Mockito.mock(Interaction.class);
        val publishingMmc = newMetamodelContextWithServices(commandPublisher, interactionProvider);
        publishingMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        val publishingLeaseSpec = publishingMmc.getSpecificationLoader().loadSpecification(Lease.class);
        val publishingSelectorAction = publishingLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").orElseThrow();
        val lease = new Lease();
        val matchingItem = new LeaseItem("first", 1, null);
        lease.getItems().add(matchingItem);
        val leaseAdapter = publishingMmc.getObjectManager().adapt(lease);
        val command = commandFor(publishingSelectorAction, leaseAdapter);
        Mockito.when(interaction.getCommand()).thenReturn(command);
        Mockito.when(interactionProvider.currentInteraction()).thenReturn(Optional.of(interaction));
        Mockito.when(interactionProvider.currentInteractionElseFail()).thenReturn(interaction);

        val result = publishingSelectorAction.getFacet(ActionInvocationFacet.class).invoke(
                publishingSelectorAction,
                publishingSelectorAction.interactionHead(leaseAdapter),
                arguments(publishingSelectorAction, publishingMmc, leaseAdapter, "first", 1, null, null, null),
                InteractionInitiatedBy.USER);

        assertThat(result.getPojo(), is(matchingItem));
        assertThat(command.getPublishingPhase(), is(Command.CommandPublishingPhase.READY));
        Mockito.verify(commandPublisher).ready(command);
        assertThat(command.getCommandDto().getMember(), instanceOf(ActionDto.class));
        assertThat(command.getCommandDto().getMember().getLogicalMemberIdentifier(),
                is(IdentifierUtil.logicalMemberIdentifierFor(publishingSelectorAction.interactionHead(leaseAdapter), publishingSelectorAction)));
        val exportDto = CommandDtoUtils.CommandExportDto.of(command.getCommandDto(), command.getResult());
        assertThat(exportDto.getCommand(), is(command.getCommandDto()));
    }

    @Test
    void selector_invocation_does_not_prepare_command_when_safe_action_publishing_disabled() {
        val commandPublisher = Mockito.mock(CommandPublisher.class);
        val interactionProvider = Mockito.mock(InteractionProvider.class);
        val interaction = Mockito.mock(Interaction.class);
        val publishingMmc = newMetamodelContextWithServices(commandPublisher, interactionProvider);
        publishingMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        val publishingLeaseSpec = publishingMmc.getSpecificationLoader().loadSpecification(Lease.class);
        val publishingSelectorAction = publishingLeaseSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil.ACTION_ID_PREFIX + "items").orElseThrow();
        publishingMmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.DISABLED);
        val lease = new Lease();
        lease.getItems().add(new LeaseItem("first", 1, null));
        val leaseAdapter = publishingMmc.getObjectManager().adapt(lease);
        val command = commandFor(publishingSelectorAction, leaseAdapter);
        Mockito.when(interaction.getCommand()).thenReturn(command);
        Mockito.when(interactionProvider.currentInteraction()).thenReturn(Optional.of(interaction));
        Mockito.when(interactionProvider.currentInteractionElseFail()).thenReturn(interaction);

        publishingSelectorAction.getFacet(ActionInvocationFacet.class).invoke(
                publishingSelectorAction,
                publishingSelectorAction.interactionHead(leaseAdapter),
                arguments(publishingSelectorAction, publishingMmc, leaseAdapter, "first", 1, null, null, null),
                InteractionInitiatedBy.USER);

        assertThat(command.getPublishingPhase(), is(Command.CommandPublishingPhase.ONHOLD));
        Mockito.verify(commandPublisher).ready(command);
    }

    private void assertSelectorActionStyling(final ObjectAction action) {
        val cssClassFacet = action.getFacet(CssClassFacet.class);
        assertThat(cssClassFacet, instanceOf(CssClassFacetForParentedCollectionSelector.class));
        assertThat(cssClassFacet.cssClass(null), is(CssClassFacetForParentedCollectionSelector.CSS_CLASS));

        val faFacet = action.getFacet(FaFacet.class);
        assertThat(faFacet, instanceOf(FaFacetForParentedCollectionSelector.class));
        assertThat(faFacet.getSpecialization().leftIfAny().getLayers().toQuickNotation(), is(FaFacetForParentedCollectionSelector.CSS_CLASS_FA));
    }

    private MetaModelContext_forTesting newMetamodelContext(final Class<?>... mixinTypes) {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .causewayBeanTypeRegistry(new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                        .map(mixinType -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, mixinType))))
                .build();
    }

    private MetaModelContext_forTesting newMetamodelContextWithServices(
            final CommandPublisher commandPublisher,
            final InteractionProvider interactionProvider) {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .singleton(commandPublisher)
                .singleton(interactionProvider)
                .build();
    }

    private Command commandFor(
            final ObjectAction action,
            final ManagedObject targetAdapter) {
        val command = new Command(UUID.randomUUID());
        command.updater().setCommandDtoAndIdentifier(commandDtoFor(action, targetAdapter, command.getInteractionId()));
        return command;
    }

    private CommandDto commandDtoFor(
            final ObjectAction action,
            final ManagedObject targetAdapter,
            final UUID interactionId) {
        val commandDto = new CommandDto();
        commandDto.setInteractionId(interactionId.toString());
        val actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(IdentifierUtil.logicalMemberIdentifierFor(action.interactionHead(targetAdapter), action));
        commandDto.setMember(actionDto);
        return commandDto;
    }

    private Consent validate(
            final Lease lease,
            final String name,
            final Integer sequence) {
        return validate(lease, name, sequence, null, null, null);
    }

    private Consent validate(
            final Lease lease,
            final String name,
            final Integer sequence,
            final BoundedReference boundedReference,
            final SelectableReference choicesReference,
            final SelectableReference autocompleteReference) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        return selectorAction.isArgumentSetValid(
                selectorAction.interactionHead(leaseAdapter),
                arguments(selectorAction, mmc, leaseAdapter, name, sequence, boundedReference, choicesReference, autocompleteReference),
                InteractionInitiatedBy.USER);
    }

    private ManagedObject executeWithRuleChecking(
            final Lease lease,
            final String name,
            final Integer sequence) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        return selectorAction.executeWithRuleChecking(
                selectorAction.interactionHead(leaseAdapter),
                arguments(selectorAction, mmc, leaseAdapter, name, sequence, null, null, null),
                InteractionInitiatedBy.USER,
                null);
    }

    private ManagedObject invoke(
            final Lease lease,
            final String name,
            final Integer sequence) {
        return invoke(lease, name, sequence, null, null, null);
    }

    private ManagedObject invoke(
            final Lease lease,
            final String name,
            final Integer sequence,
            final BoundedReference boundedReference,
            final SelectableReference choicesReference,
            final SelectableReference autocompleteReference) {
        val leaseAdapter = mmc.getObjectManager().adapt(lease);
        val actionInvocationFacet = selectorAction.getFacet(ActionInvocationFacet.class);
        return actionInvocationFacet.invoke(
                selectorAction,
                selectorAction.interactionHead(leaseAdapter),
                arguments(selectorAction, mmc, leaseAdapter, name, sequence, boundedReference, choicesReference, autocompleteReference),
                InteractionInitiatedBy.USER);
    }

    private Can<ManagedObject> arguments(
            final ManagedObject leaseAdapter,
            final String name,
            final Integer sequence) {
        return arguments(selectorAction, mmc, leaseAdapter, name, sequence, null, null, null);
    }

    private Can<ManagedObject> arguments(
            final MetaModelContext_forTesting context,
            final ManagedObject leaseAdapter,
            final String name,
            final Integer sequence) {
        return arguments(selectorAction, context, leaseAdapter, name, sequence, null, null, null);
    }

    private Can<ManagedObject> arguments(
            final ObjectAction action,
            final MetaModelContext_forTesting context,
            final ManagedObject leaseAdapter,
            final String name,
            final Integer sequence,
            final BoundedReference boundedReference,
            final SelectableReference choicesReference,
            final SelectableReference autocompleteReference) {
        return action.getParameters().stream()
                .map(parameter -> {
                    if(parameter.getId().equals("lease")) {
                        return leaseAdapter;
                    }
                    val value = valueFor(parameter.getId(), name, sequence, boundedReference, choicesReference, autocompleteReference);
                    return value != null
                            ? context.getObjectManager().adapt(value)
                            : ManagedObject.empty(parameter.getElementType());
                })
                .collect(Can.toCan());
    }

    private Object valueFor(
            final String parameterId,
            final String name,
            final Integer sequence,
            final BoundedReference boundedReference,
            final SelectableReference choicesReference,
            final SelectableReference autocompleteReference) {
        switch (parameterId) {
        case "name":
            return name;
        case "sequence":
            return sequence;
        case "boundedReference":
            return boundedReference;
        case "choicesReference":
            return choicesReference;
        case "autocompleteReference":
            return autocompleteReference;
        default:
            return null;
        }
    }

}
