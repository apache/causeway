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
package org.apache.causeway.viewer.graphql.model.domain.common.query.meta;

import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.application.ApplicationEntryService;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CommonMetaBreadcrumbsTest {

    @Test
    void usesOneSharedEntryTypeWithRequiredIdentityAndTitle() {
        final var context = context();
        final var first = new CommonMetaBreadcrumbs(context);
        final var second = new CommonMetaBreadcrumbs(context);

        assertThat(first.getField().getName()).isEqualTo("breadcrumbs");
        assertThat(first.getField().getType().toString()).isEqualTo("[RichNavigableBreadcrumb!]");
        assertThat(second.getField().getType().toString()).isEqualTo(first.getField().getType().toString());
        assertThat(context.graphQLTypeRegistry.getGraphQLTypes())
                .filteredOn(type -> type.toString().contains(CommonMetaBreadcrumbs.ENTRY_TYPE_NAME))
                .hasSize(1);
        final var entry = context.graphQLTypeRegistry.lookup(
                CommonMetaBreadcrumbs.ENTRY_TYPE_NAME,
                graphql.schema.GraphQLObjectType.class).orElseThrow();
        assertThat(entry.getFieldDefinitions())
                .extracting(field -> field.getName() + ":" + field.getType())
                .containsExactly(
                        "logicalTypeName:String!",
                        "id:String!",
                        "title:String!");
    }

    private static Context context() {
        @SuppressWarnings("unchecked")
        final Provider<Context> contextProvider = mock(Provider.class);
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
