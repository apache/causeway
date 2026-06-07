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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.CssClassFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForNullScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.FaFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutGroupFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacetDefault;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.val;

class ScalarReferenceNavigationActionUtilTest {

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class ReferenceOwner {
        private ReferencedObject reference;
        private String value;

        public ReferencedObject getReference() {
            return reference;
        }

        public String getValue() {
            return value;
        }
    }

    @DomainObject(nature = Nature.ENTITY)
    static class EntityReferenceOwner {
        private ReferencedObject reference;

        public ReferencedObject getReference() {
            return reference;
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class SuppressedReferenceOwner implements CommandRecordingSuppressed {
        private ReferencedObject reference;

        public ReferencedObject getReference() {
            return reference;
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    static class ReferencedObject {
    }

    private MetaModelContext mmc;
    private ObjectSpecification ownerSpec;
    private ObjectAction navigationAction;

    @BeforeEach
    void setUp() {
        mmc = newMetamodelContext();
        mmc.getConfiguration().getCore().getMetaModel().setFilterVisibility(false);
        mmc.getConfiguration().getExtensions().getCommandLog().setRecordingSupport(RecordingSupport.ENABLED);
        ownerSpec = mmc.getSpecificationLoader().loadSpecification(ReferenceOwner.class);
        navigationAction = ownerSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX + "reference").orElseThrow();
    }

    @Test
    void does_not_synthesize_reference_navigation_action_unless_enabled() {
        val disabledMmc = newMetamodelContext();
        val disabledOwnerSpec = disabledMmc.getSpecificationLoader().loadSpecification(ReferenceOwner.class);

        assertThat(disabledOwnerSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX + "reference").isPresent(), is(false));
    }

    @Test
    void synthesizes_reference_navigation_action_with_marker_safe_semantics_and_command_publishing_when_enabled() {
        assertThat(navigationAction.getId(), is("__causeway_navigate_to_reference"));
        MatcherAssert.assertThat(navigationAction.getFacet(ScalarReferenceNavigationFacet.class), instanceOf(ScalarReferenceNavigationFacetDefault.class));
        assertThat(navigationAction.getSemantics(), is(SemanticsOf.SAFE));
        assertThat(navigationAction.getFacet(CommandPublishingFacet.class).isEnabled(), is(true));
        assertThat(navigationAction.getParameters().size(), is(0));
    }

    @Test
    void synthesizes_reference_navigation_action_for_entity_owned_reference() {
        val entityOwnerSpec = mmc.getSpecificationLoader().loadSpecification(EntityReferenceOwner.class);
        val entityNavigationAction = entityOwnerSpec.getAction(
                ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX + "reference").orElseThrow();

        assertThat(entityNavigationAction.getId(), is("__causeway_navigate_to_reference"));
        assertThat(entityNavigationAction.getFacet(ScalarReferenceNavigationFacet.class), instanceOf(ScalarReferenceNavigationFacetDefault.class));
    }

    @Test
    void does_not_synthesize_reference_navigation_action_for_suppressed_owner_type() {
        val suppressedSpec = mmc.getSpecificationLoader().loadSpecification(SuppressedReferenceOwner.class);

        assertThat(suppressedSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX + "reference").isPresent(), is(false));
    }

    @Test
    void does_not_synthesize_reference_navigation_action_for_scalar_value_property() {
        assertThat(ownerSpec.getAction(ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil.ACTION_ID_PREFIX + "value").isPresent(), is(false));
    }

    @Test
    void associates_reference_navigation_action_with_scalar_reference() {
        val layoutGroupFacet = navigationAction.getFacet(LayoutGroupFacet.class);

        assertThat(layoutGroupFacet, instanceOf(LayoutGroupFacetForScalarReferenceNavigation.class));
        assertThat(layoutGroupFacet.getGroupId(), is("reference"));
        assertThat(layoutGroupFacet.getGroupName(), is("Reference"));
    }

    @Test
    void names_and_styles_reference_navigation_action_as_navigate_to() {
        assertThat(navigationAction.getCanonicalFriendlyName(), is("Navigate To"));

        val cssClassFacet = navigationAction.getFacet(CssClassFacet.class);
        assertThat(cssClassFacet, instanceOf(CssClassFacetForParentedCollectionNavigation.class));
        assertThat(cssClassFacet.cssClass(null), is(CssClassFacetForParentedCollectionNavigation.CSS_CLASS));

        val faFacet = navigationAction.getFacet(FaFacet.class);
        assertThat(faFacet, instanceOf(FaFacetForParentedCollectionNavigation.class));
        assertThat(faFacet.getSpecialization().leftIfAny().getLayers().toQuickNotation(), is(FaFacetForParentedCollectionNavigation.CSS_CLASS_FA));
    }

    @Test
    void disables_reference_navigation_action_when_associated_reference_is_null() {
        val owner = new ReferenceOwner();
        val ownerAdapter = mmc.getObjectManager().adapt(owner);

        val consent = navigationAction.isUsable(ownerAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE);

        assertThat(navigationAction.getFacet(DisabledFacet.class), instanceOf(DisabledFacetForNullScalarReferenceNavigation.class));
        assertThat(consent.isVetoed(), is(true));
        assertThat(consent.getReasonAsString().orElseThrow(), containsString(DisabledFacetForNullScalarReferenceNavigation.REASON));
    }

    @Test
    void leaves_reference_navigation_action_enabled_when_associated_reference_is_not_null() {
        val owner = new ReferenceOwner();
        owner.reference = new ReferencedObject();
        val ownerAdapter = mmc.getObjectManager().adapt(owner);

        assertThat(navigationAction.isUsable(ownerAdapter, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed(), is(true));
    }

    @Test
    void invokes_reference_navigation_action_returning_referenced_object() {
        val owner = new ReferenceOwner();
        val referencedObject = new ReferencedObject();
        owner.reference = referencedObject;
        val ownerAdapter = mmc.getObjectManager().adapt(owner);

        val result = invoke(ownerAdapter);

        assertThat(navigationAction.getFacet(ActionInvocationFacet.class), instanceOf(ActionInvocationFacetForScalarReferenceNavigation.class));
        assertThat(result.getPojo(), is(referencedObject));
    }

    @Test
    void direct_invocation_fails_when_reference_is_null() {
        val ownerAdapter = mmc.getObjectManager().adapt(new ReferenceOwner());

        val ex = assertThrows(RecoverableException.class, () -> invoke(ownerAdapter));

        assertThat(ex.getMessage(), containsString(DisabledFacetForNullScalarReferenceNavigation.REASON));
    }

    private ManagedObject invoke(final ManagedObject ownerAdapter) {
        return navigationAction.getFacet(ActionInvocationFacet.class).invoke(
                navigationAction,
                navigationAction.interactionHead(ownerAdapter),
                Can.empty(),
                InteractionInitiatedBy.USER);
    }

    private MetaModelContext newMetamodelContext() {
        return MetaModelContext_forTesting.buildDefault();
    }

}
