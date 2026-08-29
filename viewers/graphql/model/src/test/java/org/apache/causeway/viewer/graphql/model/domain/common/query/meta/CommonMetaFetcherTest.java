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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonMetaFetcherTest {

    @Test
    void returnsEmptySingleAndRootOrderedAncestorChains() {
        final var bookmarks = Map.of(
                "owner", bookmark("Owner", "owner-1"),
                "pet", bookmark("Pet", "pet-1"),
                "visit", bookmark("Visit", "visit-1"));
        assertThat(traverse("owner", Map.of(), bookmarks)).isEmpty();
        assertThat(traverse("pet", Map.of("pet", "owner"), bookmarks))
                .containsExactly(Map.of(
                        "logicalTypeName", "test.Owner",
                        "id", "owner-1",
                        "title", "Title owner"));
        assertThat(traverse("visit", Map.of("visit", "pet", "pet", "owner"), bookmarks))
                .extracting(entry -> entry.get("id"))
                .containsExactly("owner-1", "pet-1");
    }

    @Test
    void stopsBeforeAnUnbookmarkableParent() {
        assertThat(CommonMetaFetcher.traverseBreadcrumbs(
                bookmark("Child", "child-1"),
                "child",
                pojo -> "child".equals(pojo) ? "transient" : null,
                pojo -> Optional.empty(),
                pojo -> "secret transient title"))
                .isEmpty();
    }

    @Test
    void rejectsCyclesDepthOverflowAndThrowingParentWithoutSensitiveDetails() {
        final var cyclicParents = Map.of("child", "parent", "parent", "child");
        assertThatThrownBy(() -> CommonMetaFetcher.traverseBreadcrumbs(
                bookmark("Child", "child-1"),
                "child",
                cyclicParents::get,
                pojo -> Optional.of(bookmark(capitalize((String) pojo), pojo + "-1")),
                pojo -> "Title " + pojo))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GRAPHQL_BREADCRUMB_CYCLE");

        final var parents = new HashMap<Object, Object>();
        for (int index = 0; index <= CommonMetaFetcher.MAXIMUM_BREADCRUMB_ANCESTORS; index++) {
            parents.put(index, index + 1);
        }
        assertThatThrownBy(() -> CommonMetaFetcher.traverseBreadcrumbs(
                bookmark("Node", "0"),
                0,
                parents::get,
                pojo -> Optional.of(bookmark("Node", pojo.toString())),
                pojo -> "Node " + pojo))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GRAPHQL_BREADCRUMB_DEPTH_EXCEEDED")
                .hasMessageContaining("32");

        assertThatThrownBy(() -> CommonMetaFetcher.traverseBreadcrumbs(
                bookmark("Child", "child-1"),
                "child",
                pojo -> {
                    throw new IllegalArgumentException("secret domain exception");
                },
                pojo -> Optional.empty(),
                Object::toString))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GRAPHQL_BREADCRUMB_PARENT_FAILED")
                .hasMessageNotContaining("secret domain exception");
    }

    private static java.util.List<Map<String, String>> traverse(
            final String current,
            final Map<String, String> parents,
            final Map<String, Bookmark> bookmarks) {
        return CommonMetaFetcher.traverseBreadcrumbs(
                bookmarks.get(current),
                current,
                parents::get,
                pojo -> Optional.ofNullable(bookmarks.get(pojo)),
                pojo -> "Title " + pojo);
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("test." + type, id);
    }

    private static String capitalize(final String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
