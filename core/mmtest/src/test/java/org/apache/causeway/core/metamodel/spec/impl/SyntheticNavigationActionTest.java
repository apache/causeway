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
package org.apache.causeway.core.metamodel.spec.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.CssClassFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForEmptyParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForNullScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.FaFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParentedCollectionNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.util.TestPropertyValues;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

class SyntheticNavigationActionTest {

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class Lease {
        @Getter
        @CollectionLayout(sequence = "42")
        private final List<LeaseItem> items = new ArrayList<>();
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class SuppressedLease implements CommandRecordingSuppressed {
        @Getter
        private final List<LeaseItem> items = new ArrayList<>();
    }

    @DomainObject(nature = Nature.ENTITY)
    static class EntityLease {
        @Getter
        private final List<LeaseItem> items = new ArrayList<>();
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class AbstractItemOwner {
        @Getter
        private final List<AbstractItem> items = new ArrayList<>();
    }

    @DomainObject
    abstract static class AbstractItem {
        @Getter
        private String name;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class LeaseItem {
        @Getter
        @PropertyLayout(sequence = "1")
        private final String name;
        @Getter
        @PropertyLayout(sequence = "2")
        private Blob attachment;
        @Getter
        @PropertyLayout(sequence = "3")
        private final boolean checkbox;
        @Getter
        @PropertyLayout(sequence = "4")
        private final Integer sequence;
        @Getter
        @PropertyLayout(sequence = "5")
        private final Category category;
        @Getter
        @PropertyLayout(sequence = "6")
        private BoundedReference boundedReference;
        @Getter
        @PropertyLayout(sequence = "7")
        private AutocompleteReference autocompleteReference;
        @Getter
        @PropertyLayout(sequence = "8")
        private ObjectAutocompleteReference objectAutocompleteReference;
        @Getter
        @PropertyLayout(sequence = "9", hidden = Where.PARENTED_TABLES)
        private String internalCode;
        @Getter
        @PropertyLayout(sequence = "10")
        private final UnconstrainedReference unconstrainedReference;

        @MemberSupport
        public List<Category> choicesCategory() {
            return List.of();
        }

        @MemberSupport
        public List<AutocompleteReference> autoCompleteAutocompleteReference(final String search) {
            return List.of();
        }
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class Category {
        @Getter
        private final String name;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL, bounding = Bounding.BOUNDED)
    static class BoundedReference {
        @Getter
        private final String name;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class AutocompleteReference {
        @Getter
        private final String name;
    }

    static class ObjectAutocompleteReferenceRepository {
        public List<ObjectAutocompleteReference> autoComplete(final String search) {
            return List.of();
        }
    }

    @RequiredArgsConstructor
    @DomainObject(
            nature = Nature.VIEW_MODEL,
            autoCompleteRepository = ObjectAutocompleteReferenceRepository.class)
    static class ObjectAutocompleteReference {
        @Getter
        private final String name;
    }

    @RequiredArgsConstructor
    @DomainObject(nature = Nature.VIEW_MODEL)
    static class UnconstrainedReference {
        @Getter
        private final String name;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class ReferenceOwner {
        @Getter
        private Lease reference;
        @Getter
        private String value;
    }

    @DomainObject(nature = Nature.ENTITY)
    static class EntityReferenceOwner {
        @Getter
        private Lease reference;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class AbstractReferenceOwner {
        @Getter
        private AbstractItem reference;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class SuppressedReferenceOwner implements CommandRecordingSuppressed {
        @Getter
        private Lease reference;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class ReservedIdCollisionOwner {
        @Getter
        private Lease reference;

        public Lease __causeway_navigate_to_reference() {
            return reference;
        }
    }

    @Test
    void registered_postprocessor_synthesizes_actions_before_later_action_processing() {
        var mmc = context(true, true);
        var spec = mmc.getSpecificationLoader().specForTypeElseFail(Lease.class);

        assertThat(spec.getAction("__causeway_navigate_to_items")).isPresent();
    }

    @Test
    void synthesis_is_configuration_gated_and_suppressed_by_owner_marker() {
        var disabled = context(false);
        assertThat(action(disabled, Lease.class, "items")).isEmpty();

        var enabled = context(true);
        assertThat(action(enabled, Lease.class, "items")).isPresent();
        assertThat(action(enabled, EntityLease.class, "items")).isPresent();
        assertThat(action(enabled, AbstractItemOwner.class, "items")).isPresent();
        assertThat(action(enabled, EntityReferenceOwner.class, "reference")).isPresent();
        assertThat(action(enabled, AbstractReferenceOwner.class, "reference")).isPresent();
        assertThat(action(enabled, SuppressedLease.class, "items")).isEmpty();
        assertThat(action(enabled, SuppressedReferenceOwner.class, "reference")).isEmpty();
    }

    @Test
    void repeated_synthesis_is_an_idempotent_no_op() {
        var mmc = context(true);
        var spec = mmc.getSpecificationLoader().specForTypeElseFail(Lease.class);
        Assertions.assertTrue(((ObjectSpecificationBuilder)spec).isFullyIntrospected());
        assertThat(spec.streamRuntimeActions(MixedIn.INCLUDED)
                .filter(action -> action.getId().equals("__causeway_navigate_to_items"))
                .count()).isEqualTo(1L);
    }

    @Test
    void collection_action_has_stable_safe_framework_metadata_and_normal_publication() {
        var action = action(context(true), Lease.class, "items").orElseThrow();

        assertThat(action.getId()).isEqualTo("__causeway_navigate_to_items");
        assertThat(action.getCanonicalFriendlyName()).isEqualTo("Navigate To");
        assertThat(action.getSemantics()).isEqualTo(SemanticsOf.SAFE);
        assertThat(action.lookupFacet(ParentedCollectionNavigationFacet.class)).isPresent();
        assertThat(action.lookupFacet(CommandPublishingFacet.class).orElseThrow().isEnabled()).isTrue();
        assertThat(action.lookupFacet(LayoutGroupFacet.class).orElseThrow().getGroupId()).isEqualTo("items");
        assertThat(action.lookupFacet(LayoutOrderFacet.class).orElseThrow().getSequence()).isEqualTo("42");
        assertThat(action.lookupFacet(CssClassFacet.class).orElseThrow().cssClass(null))
                .isEqualTo(CssClassFacetForParentedCollectionNavigation.CSS_CLASS);
        assertThat(action.lookupFacet(FaFacet.class).orElseThrow().getSpecialization().leftIfAny()
                .getLayers().toQuickNotation())
                .isEqualTo(FaFacetForParentedCollectionNavigation.CSS_CLASS_FA);
    }

    @Test
    void collection_parameters_follow_columns_and_exclude_hidden_large_and_unconstrained_properties() {
        var action = action(context(true), Lease.class, "items").orElseThrow();

        assertThat(action.getParameters().stream().map(ObjectActionParameter::getId).toList())
                .containsExactly(
                        "name",
                        "checkbox",
                        "sequence",
                        "category",
                        "boundedReference",
                        "autocompleteReference",
                        "objectAutocompleteReference");
        assertThat(action.getParameters().stream().allMatch(ObjectActionParameter::isOptional)).isTrue();
        assertThat(action.getParameters().stream()
                .filter(parameter -> parameter.getId().equals("checkbox"))
                .findFirst().orElseThrow().getElementType().getCorrespondingClass())
                .isEqualTo(Boolean.class);
    }

    @Test
    void collection_usability_validation_and_invocation_share_exactly_one_matching_rule() {
        var mmc = context(true);
        var action = action(mmc, Lease.class, "items").orElseThrow();
        var lease = new Lease();
        var category = new Category("A");
        var first = new LeaseItem("first child", true, 1, category, new UnconstrainedReference("x"));
        var second = new LeaseItem(
                "second child", false, 2, new Category("B"), new UnconstrainedReference("y"));
        var leaseAdapter = mmc.getObjectManager().adapt(lease);

        assertThat(action.lookupFacet(DisabledFacet.class).orElseThrow())
                .isInstanceOf(DisabledFacetForEmptyParentedCollectionNavigation.class);
        assertThat(action.isUsable(leaseAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isVetoed()).isTrue();

        lease.getItems().add(first);
        lease.getItems().add(second);
        assertThat(action.isUsable(leaseAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed()).isTrue();

        var firstArgs = arguments(mmc, action, Map.of("name", "first"));
        assertThat(action.isArgumentSetValid(
                action.interactionHead(leaseAdapter), firstArgs, InteractionInitiatedBy.USER).isAllowed()).isTrue();
        assertThat(invoke(action, leaseAdapter, firstArgs).getPojo()).isSameAs(first);

        var falseArgs = arguments(mmc, action, Map.of("checkbox", false));
        assertThat(invoke(action, leaseAdapter, falseArgs).getPojo()).isSameAs(second);

        var referenceArgs = arguments(mmc, action, Map.of("category", category));
        assertThat(invoke(action, leaseAdapter, referenceArgs).getPojo()).isSameAs(first);

        var noMatchArgs = arguments(mmc, action, Map.of("name", "missing"));
        assertThat(action.isArgumentSetValid(
                        action.interactionHead(leaseAdapter), noMatchArgs, InteractionInitiatedBy.USER)
                .getReasonAsString().orElseThrow()).contains("0 items match");
        assertThatThrownBy(() -> invoke(action, leaseAdapter, noMatchArgs))
                .isInstanceOf(RecoverableException.class)
                .hasMessageContaining("0 items match");

        var noFilters = arguments(mmc, action, Map.of());
        assertThat(action.isArgumentSetValid(
                        action.interactionHead(leaseAdapter), noFilters, InteractionInitiatedBy.USER)
                .getReasonAsString().orElseThrow()).contains("2 items match");
        assertThatThrownBy(() -> invoke(action, leaseAdapter, noFilters))
                .isInstanceOf(RecoverableException.class)
                .hasMessageContaining("2 items match");
    }

    @Test
    void reference_action_is_parameterless_disabled_for_null_and_returns_reference() {
        var mmc = context(true);
        var action = action(mmc, ReferenceOwner.class, "reference").orElseThrow();
        var owner = new ReferenceOwner();
        var ownerAdapter = mmc.getObjectManager().adapt(owner);

        assertThat(action.getId()).isEqualTo("__causeway_navigate_to_reference");
        assertThat(action.getParameters().isEmpty()).isTrue();
        assertThat(action.getCanonicalFriendlyName()).isEqualTo("Navigate To");
        assertThat(action.getSemantics()).isEqualTo(SemanticsOf.SAFE);
        assertThat(action.lookupFacet(ScalarReferenceNavigationFacet.class)).isPresent();
        assertThat(action.lookupFacet(LayoutGroupFacet.class).orElseThrow().getGroupId()).isEqualTo("reference");
        assertThat(action.lookupFacet(CommandPublishingFacet.class).orElseThrow().isEnabled()).isTrue();
        assertThat(action.lookupFacet(DisabledFacet.class).orElseThrow())
                .isInstanceOf(DisabledFacetForNullScalarReferenceNavigation.class);
        assertThat(action.lookupFacet(ActionInvocationFacet.class).orElseThrow())
                .isInstanceOf(ActionInvocationFacetForScalarReferenceNavigation.class);
        assertThat(action.isUsable(ownerAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isVetoed()).isTrue();
        assertThatThrownBy(() -> invoke(action, ownerAdapter, Can.empty()))
                .isInstanceOf(RecoverableException.class)
                .hasMessageContaining(DisabledFacetForNullScalarReferenceNavigation.REASON);

        var reference = new Lease();
        owner.reference = reference;
        assertThat(action.isUsable(ownerAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed()).isTrue();
        assertThat(invoke(action, ownerAdapter, Can.empty()).getPojo()).isSameAs(reference);
    }

    @Test
    void invocation_facets_do_not_publish_outside_the_normal_executor_path() {
        var commandPublisher = Mockito.mock(CommandPublisher.class);
        var mmc = MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .singleton(commandPublisher)
                .testPropertyValues(TestPropertyValues.of(
                        "causeway.extensions.command-log.recording-support=ENABLED"))
                .build();
        var action = action(mmc, Lease.class, "items").orElseThrow();
        var lease = new Lease();
        var child = new LeaseItem(
                "only", true, 1, new Category("A"), new UnconstrainedReference("x"));
        lease.getItems().add(child);
        var target = mmc.getObjectManager().adapt(lease);

        assertThat(invoke(action, target, arguments(mmc, action, Map.of())).getPojo()).isSameAs(child);
        Mockito.verifyNoInteractions(commandPublisher);
    }

    @Test
    void scalar_value_property_does_not_gain_navigation_action() {
        assertThat(action(context(true), ReferenceOwner.class, "value")).isEmpty();
    }

    @Test
    void reserved_action_id_collision_fails_instead_of_replacing_authored_action() {
        assertThatThrownBy(() -> action(context(true), ReservedIdCollisionOwner.class, "reference"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reserved for synthetic navigation");
    }

    private static MetaModelContext_forTesting context(final boolean enabled) {
        return context(enabled, false);
    }

    private static MetaModelContext_forTesting context(
            final boolean enabled,
            final boolean enablePostprocessors) {
        return MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .enablePostprocessors(enablePostprocessors)
                .testPropertyValues(TestPropertyValues.of(
                        "causeway.extensions.command-log.recording-support=" + (enabled ? "ENABLED" : "DISABLED")))
                .build();
    }

    private static java.util.Optional<ObjectAction> action(
            final MetaModelContext_forTesting mmc,
            final Class<?> ownerType,
            final String associationId) {
        ObjectSpecification spec = mmc.getSpecificationLoader().specForTypeElseFail(ownerType);
        Assertions.assertTrue(((ObjectSpecificationBuilder)spec).isFullyIntrospected());
        return spec.getAction(SyntheticNavigationActionFactory.ACTION_ID_PREFIX + associationId);
    }

    private static Can<ManagedObject> arguments(
            final MetaModelContext_forTesting mmc,
            final ObjectAction action,
            final Map<String, Object> valuesById) {
        return action.getParameters().stream()
                .map(parameter -> {
                    var value = valuesById.get(parameter.getId());
                    return value == null
                            ? ManagedObject.empty(parameter.getElementType())
                            : mmc.getObjectManager().adapt(value);
                })
                .collect(Can.toCan());
    }

    private static ManagedObject invoke(
            final ObjectAction action,
            final ManagedObject target,
            final Can<ManagedObject> arguments) {
        return action.lookupFacet(ActionInvocationFacet.class).orElseThrow().invoke(
                action,
                action.interactionHead(target),
                arguments,
                InteractionInitiatedBy.USER);
    }
}
