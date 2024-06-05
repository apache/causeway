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
package org.apache.causeway.applib.services.search;

import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import lombok.NonNull;

/**
 * EXPERIMENTAL/DRAFT
 * <p>
 * If a {@link CollectionSearchService} is registered with Spring's context,
 * viewer implementations (like Wicket Viewer) should show
 * a quick-search prompt, which is rendered on top of the UI table that
 * presents the collection in question.
 *
 * @since 2.1, 3.1 {@index}
 */
public interface CollectionSearchService {

    /**
     * Optionally returns a {@link Predicate} that filters collections
     * of given {@code domainType} by a nullable {@code searchString},
     * based on whether the search is supported.
     * <p>
     * For example, the searchString could be parsed into tokens, and then matched against the
     * domain object's title say.
     *
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @param searchString - nullable, e.g. the searchString could be parsed into tokens, and then matched against the
     *      domain object's title say
     */
    <T> Optional<Predicate<T>> searchFilter(
            @NonNull Class<T> domainType,
            @Nullable String searchString);

}