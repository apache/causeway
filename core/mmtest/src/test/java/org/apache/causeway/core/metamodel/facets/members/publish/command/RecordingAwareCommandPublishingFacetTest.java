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
package org.apache.causeway.core.metamodel.facets.members.publish.command;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.config.metamodel.facets.PropertyConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordingAwareCommandPublishingFacetTest {

    @Test
    void enabledRecordingPublishesUnannotatedSafeActionDespiteNonePolicy() {
        var facet = actionFacet(
                RecordingSupport.ENABLED,
                ActionConfigOptions.PublishingPolicy.NONE,
                SemanticsOf.SAFE,
                Publishing.NOT_SPECIFIED);

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForActionFromConfiguration.SafeEnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingPublishesAsConfiguredSafeActionDespiteIgnoreSafePolicy() {
        var facet = actionFacet(
                RecordingSupport.ENABLED,
                ActionConfigOptions.PublishingPolicy.IGNORE_SAFE,
                SemanticsOf.SAFE,
                Publishing.AS_CONFIGURED);

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForActionFromConfiguration.SafeEnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void disabledRecordingPreservesSafeActionDefault() {
        var facet = actionFacet(
                RecordingSupport.DISABLED,
                ActionConfigOptions.PublishingPolicy.NONE,
                SemanticsOf.SAFE,
                Publishing.NOT_SPECIFIED);

        assertThat(facet.isEnabled()).isFalse();
    }

    @Test
    void disabledRecordingPreservesGlobalAllForSafeAction() {
        var facet = actionFacet(
                RecordingSupport.DISABLED,
                ActionConfigOptions.PublishingPolicy.ALL,
                SemanticsOf.SAFE,
                Publishing.NOT_SPECIFIED);

        assertThat(facet).isInstanceOf(CommandPublishingFacetForActionFromConfiguration.All.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void explicitSafeActionDisablementRemainsAuthoritative() {
        var facet = actionFacet(
                RecordingSupport.ENABLED,
                ActionConfigOptions.PublishingPolicy.ALL,
                SemanticsOf.SAFE,
                Publishing.DISABLED);

        assertThat(facet).isInstanceOf(CommandPublishingFacetForActionAnnotation.Disabled.class);
        assertThat(facet.isEnabled()).isFalse();
    }

    @Test
    void explicitSafeActionEnablementUsesOneExistingFacet() {
        var facet = actionFacet(
                RecordingSupport.ENABLED,
                ActionConfigOptions.PublishingPolicy.NONE,
                SemanticsOf.SAFE,
                Publishing.ENABLED);

        assertThat(facet).isExactlyInstanceOf(CommandPublishingFacetForActionAnnotation.Enabled.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void commandDtoProcessorRetainsExistingSafeActionEnablement() {
        var holder = safeHolder(SemanticsOf.SAFE);
        var action = mock(Action.class);
        when(action.commandPublishing()).thenReturn(Publishing.DISABLED);
        doReturn(CommandDtoProcessor.Null.class).when(action).commandDtoProcessor();

        var facet = CommandPublishingFacetForActionAnnotation.create(
                Optional.of(action),
                configuration(
                        RecordingSupport.ENABLED,
                        ActionConfigOptions.PublishingPolicy.NONE,
                        PropertyConfigOptions.PublishingPolicy.NONE),
                null,
                holder)
                .orElseThrow();

        assertThat(facet).isExactlyInstanceOf(CommandPublishingFacetForActionAnnotation.Enabled.class);
        assertThat(facet.getProcessor()).isInstanceOf(CommandDtoProcessor.Null.class);
    }

    @Test
    void enabledRecordingDoesNotBroadenNonSafeActionPolicy() {
        var facet = actionFacet(
                RecordingSupport.ENABLED,
                ActionConfigOptions.PublishingPolicy.NONE,
                SemanticsOf.IDEMPOTENT,
                Publishing.AS_CONFIGURED);

        assertThat(facet).isInstanceOf(CommandPublishingFacetForActionAnnotationAsConfigured.None.class);
        assertThat(facet.isEnabled()).isFalse();
    }

    @Test
    void enabledRecordingPublishesUnannotatedProperty() {
        var facet = propertyFacet(
                RecordingSupport.ENABLED,
                PropertyConfigOptions.PublishingPolicy.NONE,
                Publishing.NOT_SPECIFIED,
                propertyHolder());

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForPropertyFromConfiguration.EnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingOverridesAsConfiguredPropertyPolicy() {
        var facet = propertyFacet(
                RecordingSupport.ENABLED,
                PropertyConfigOptions.PublishingPolicy.NONE,
                Publishing.AS_CONFIGURED,
                propertyHolder());

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForPropertyFromConfiguration.EnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingOverridesExplicitPropertyDisablement() {
        var facet = propertyFacet(
                RecordingSupport.ENABLED,
                PropertyConfigOptions.PublishingPolicy.NONE,
                Publishing.DISABLED,
                propertyHolder());

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForPropertyFromConfiguration.EnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingUsesOneFacetForAlreadyPublishedProperty() {
        var facet = propertyFacet(
                RecordingSupport.ENABLED,
                PropertyConfigOptions.PublishingPolicy.ALL,
                Publishing.ENABLED,
                propertyHolder());

        assertThat(facet).isExactlyInstanceOf(
                CommandPublishingFacetForPropertyFromConfiguration.EnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void disabledRecordingPreservesExplicitPropertyDisablement() {
        var facet = propertyFacet(
                RecordingSupport.DISABLED,
                PropertyConfigOptions.PublishingPolicy.ALL,
                Publishing.DISABLED,
                propertyHolder());

        assertThat(facet).isInstanceOf(CommandPublishingFacetForPropertyAnnotation.Disabled.class);
        assertThat(facet.isEnabled()).isFalse();
    }

    @Test
    void disabledRecordingPreservesAsConfiguredGlobalPropertyPolicy() {
        var facet = propertyFacet(
                RecordingSupport.DISABLED,
                PropertyConfigOptions.PublishingPolicy.ALL,
                Publishing.AS_CONFIGURED,
                propertyHolder());

        assertThat(facet).isInstanceOf(CommandPublishingFacetForPropertyAnnotationAsConfigured.All.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingPublishesContributedMixinProperty() {
        var holder = mock(FacetHolder.class);
        var contributingFacet = mock(ContributingFacet.class);
        when(contributingFacet.contributed()).thenReturn(MixinFacet.Contributing.AS_PROPERTY);
        when(holder.lookupFacet(ContributingFacet.class)).thenReturn(Optional.of(contributingFacet));

        var facet = CommandPublishingFacetForPropertyAnnotation.create(
                Optional.empty(),
                configuration(
                        RecordingSupport.ENABLED,
                        ActionConfigOptions.PublishingPolicy.NONE,
                        PropertyConfigOptions.PublishingPolicy.NONE),
                holder,
                null);

        assertThat(facet).isInstanceOf(
                CommandPublishingFacetForPropertyFromConfiguration.EnabledByRecordingSupport.class);
        assertThat(facet.isEnabled()).isTrue();
    }

    @Test
    void enabledRecordingDoesNotMakeNonPropertyFallbackPublishable() {
        var facet = CommandPublishingFacetForPropertyAnnotation.create(
                Optional.empty(),
                configuration(
                        RecordingSupport.ENABLED,
                        ActionConfigOptions.PublishingPolicy.NONE,
                        PropertyConfigOptions.PublishingPolicy.NONE),
                mock(FacetHolder.class),
                null);

        assertThat(facet).isInstanceOf(CommandPublishingFacetForActionFromConfiguration.None.class);
        assertThat(facet.isEnabled()).isFalse();
    }

    private CommandPublishingFacet actionFacet(
            final RecordingSupport recordingSupport,
            final ActionConfigOptions.PublishingPolicy actionPolicy,
            final SemanticsOf semantics,
            final Publishing publishing) {
        var holder = safeHolder(semantics);
        var action = mock(Action.class);
        when(action.commandPublishing()).thenReturn(publishing);
        doReturn(CommandDtoProcessor.class).when(action).commandDtoProcessor();

        return CommandPublishingFacetForActionAnnotation.create(
                Optional.of(action),
                configuration(recordingSupport, actionPolicy, PropertyConfigOptions.PublishingPolicy.NONE),
                null,
                holder)
                .orElseThrow();
    }

    private CommandPublishingFacet propertyFacet(
            final RecordingSupport recordingSupport,
            final PropertyConfigOptions.PublishingPolicy propertyPolicy,
            final Publishing publishing,
            final FacetHolder holder) {
        var property = mock(Property.class);
        when(property.commandPublishing()).thenReturn(publishing);
        doReturn(CommandDtoProcessor.class).when(property).commandDtoProcessor();

        return CommandPublishingFacetForPropertyAnnotation.create(
                Optional.of(property),
                configuration(recordingSupport, ActionConfigOptions.PublishingPolicy.NONE, propertyPolicy),
                holder,
                null);
    }

    private FacetHolder safeHolder(final SemanticsOf semantics) {
        var holder = mock(FacetHolder.class);
        when(holder.lookupFacet(ActionSemanticsFacet.class)).thenReturn(Optional.of(
                new ActionSemanticsFacet("test", semantics, holder)));
        return holder;
    }

    private FacetHolder propertyHolder() {
        return mock(FacetHolder.class);
    }

    private CausewayConfiguration configuration(
            final RecordingSupport recordingSupport,
            final ActionConfigOptions.PublishingPolicy actionPolicy,
            final PropertyConfigOptions.PublishingPolicy propertyPolicy) {
        var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(recordingSupport);
        when(configuration.applib().annotation().action().commandPublishing()).thenReturn(actionPolicy);
        when(configuration.applib().annotation().property().commandPublishing()).thenReturn(propertyPolicy);
        return configuration;
    }
}
