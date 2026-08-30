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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import java.util.Locale;
import java.util.Optional;

import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.context.i18n.LocaleContextHolder;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaImperativeFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaStaticFacet;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RichMemberMetadataTest {

    @Test
    void usesOneSharedTypeAndResolvesCanonicalTextOnEveryRequest() {
        var feature = mock(ObjectFeature.class);
        when(feature.getCanonicalFriendlyName()).thenAnswer(invocation ->
                LocaleContextHolder.getLocale().getLanguage().equals("fr") ? "Nom" : "Name");
        when(feature.getCanonicalDescription()).thenAnswer(invocation ->
                LocaleContextHolder.getLocale().getLanguage().equals("fr")
                        ? Optional.of("Description française")
                        : Optional.empty());
        var context = context();
        var metadata = new RichMemberMetadata(context, feature, false);

        assertThat(metadata.getField().getName()).isEqualTo("metadata");
        assertThat(metadata.getField().getType().toString()).isEqualTo("RichMemberMetadata!");
        assertThat(context.graphQLTypeRegistry.getGraphQLTypes())
                .filteredOn(type -> type.toString().contains(RichMemberMetadata.TYPE_NAME))
                .hasSize(1);

        try {
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            assertThat(metadata.fetchData(null))
                    .containsEntry("friendlyName", "Name")
                    .containsEntry("description", null)
                    .containsEntry("maxLength", null);
            LocaleContextHolder.setLocale(Locale.FRENCH);
            assertThat(metadata.fetchData(null))
                    .containsEntry("friendlyName", "Nom")
                    .containsEntry("description", "Description française");
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    @Test
    void exposesCanonicalActionInteractionMetadataOnlyForActions() {
        var destructiveAction = mock(ObjectAction.class);
        when(destructiveAction.getCanonicalFriendlyName()).thenReturn("Delete");
        when(destructiveAction.getCanonicalDescription()).thenReturn(Optional.empty());
        when(destructiveAction.getSemantics()).thenReturn(SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE);
        when(destructiveAction.getPromptStyle()).thenReturn(PromptStyle.DIALOG_MODAL);
        assertThat(new RichMemberMetadata(context(), destructiveAction, false).fetchData(null))
                .containsEntry("areYouSure", true)
                .containsEntry("promptStyle", "DIALOG_MODAL");

        var ordinaryAction = mock(ObjectAction.class);
        when(ordinaryAction.getCanonicalFriendlyName()).thenReturn("Update");
        when(ordinaryAction.getCanonicalDescription()).thenReturn(Optional.empty());
        when(ordinaryAction.getSemantics()).thenReturn(SemanticsOf.IDEMPOTENT);
        when(ordinaryAction.getPromptStyle()).thenReturn(PromptStyle.DIALOG_SIDEBAR);
        assertThat(new RichMemberMetadata(context(), ordinaryAction, false).fetchData(null))
                .containsEntry("areYouSure", false)
                .containsEntry("promptStyle", "DIALOG_SIDEBAR");

        var ordinaryFeature = mock(ObjectFeature.class);
        when(ordinaryFeature.getCanonicalFriendlyName()).thenReturn("Name");
        when(ordinaryFeature.getCanonicalDescription()).thenReturn(Optional.empty());
        assertThat(new RichMemberMetadata(context(), ordinaryFeature, false).fetchData(null))
                .containsEntry("areYouSure", null)
                .containsEntry("promptStyle", null);
    }

    @Test
    void exposesOnlyStaticActionFontAwesomeMetadata() {
        var action = mock(ObjectAction.class);
        when(action.getCanonicalFriendlyName()).thenReturn("Place order");
        when(action.getCanonicalDescription()).thenReturn(Optional.empty());
        var faFacet = mock(FaFacet.class);
        var staticFacet = mock(FaStaticFacet.class);
        when(staticFacet.getLayers()).thenReturn(FontAwesomeLayers
                .fromQuickNotation("fa-cart-shopping")
                .withPosition(CssClassFaPosition.RIGHT));
        when(faFacet.getSpecialization()).thenReturn(org.apache.causeway.commons.functional.Either.left(staticFacet));
        when(action.lookupFacet(FaFacet.class)).thenReturn(Optional.of(faFacet));

        assertThat(new RichMemberMetadata(context(), action, false).fetchData(null))
                .containsEntry("cssClassFa", "cart-shopping")
                .containsEntry("cssClassFaPosition", "RIGHT");

        var actionWithoutFacet = mock(ObjectAction.class);
        when(actionWithoutFacet.getCanonicalFriendlyName()).thenReturn("No icon");
        when(actionWithoutFacet.getCanonicalDescription()).thenReturn(Optional.empty());
        when(actionWithoutFacet.lookupFacet(FaFacet.class)).thenReturn(Optional.empty());
        assertThat(new RichMemberMetadata(context(), actionWithoutFacet, false).fetchData(null))
                .containsEntry("cssClassFa", null)
                .containsEntry("cssClassFaPosition", null);

        var imperativeAction = mock(ObjectAction.class);
        when(imperativeAction.getCanonicalFriendlyName()).thenReturn("Dynamic icon");
        when(imperativeAction.getCanonicalDescription()).thenReturn(Optional.empty());
        var imperativeFacet = mock(FaFacet.class);
        when(imperativeFacet.getSpecialization()).thenReturn(
                org.apache.causeway.commons.functional.Either.right(mock(FaImperativeFacet.class)));
        when(imperativeAction.lookupFacet(FaFacet.class)).thenReturn(Optional.of(imperativeFacet));
        assertThat(new RichMemberMetadata(context(), imperativeAction, false).fetchData(null))
                .containsEntry("cssClassFa", null)
                .containsEntry("cssClassFaPosition", null);

        var ordinaryFeature = mock(ObjectFeature.class);
        when(ordinaryFeature.getCanonicalFriendlyName()).thenReturn("Name");
        when(ordinaryFeature.getCanonicalDescription()).thenReturn(Optional.empty());
        assertThat(new RichMemberMetadata(context(), ordinaryFeature, false).fetchData(null))
                .containsEntry("cssClassFa", null)
                .containsEntry("cssClassFaPosition", null);
    }

    private static Context context() {
        @SuppressWarnings("unchecked")
        Provider<Context> contextProvider = mock(Provider.class);
        return new Context(
                mock(BookmarkService.class),
                mock(SpecificationLoader.class),
                mock(TypeMapper.class),
                mock(ServiceRegistry.class),
                mock(CausewayConfiguration.class, Answers.RETURNS_DEEP_STUBS),
                mock(CausewaySystemEnvironment.class),
                mock(ObjectManager.class),
                new GraphQLTypeRegistry(contextProvider),
                mock(ApplicationEntryService.class));
    }
}
