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

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
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
