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

import java.util.function.BiPredicate;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;

import lombok.NonNull;

/**
 * EXPERIMENTAL/DRAFT
 * <p>
 * If at least one {@link CollectionSearchService} is registered with Spring's context
 * that handles a given domainType,
 * viewer implementations (like Wicket Viewer) should show
 * a quick-search prompt, which is rendered on top of the UI table that
 * presents the collection in question.
 *
 * @since 2.1, 3.1 {@index}
 */
public interface CollectionSearchService {

    /**
     * Whether this service handles given type.
     * @param domainType - entity or view-model type to be rendered as row in a table
     */
    boolean handles(@NonNull Class<?> domainType);

    /**
     * Returns a {@link BiPredicate} that filters collections
     * of given {@code domainType} by a nullable searchString.
     * <p>
     * For example, the searchString could be parsed into tokens, and then matched against the
     * domain object's title say.
     *
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    <T> BiPredicate<T, String> searchPredicate(
            @NonNull Class<T> domainType);

    /**
     * Placeholder text for the quick-search prompt.
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    default TranslatableString searchPromptPlaceholderText(
            final @NonNull Class<?> domainType) {
        return TranslatableString.tr("Search {domainType}", "domainType", domainType.getSimpleName());
    }

    /**
     * Provides the {@link TranslationContext} for translating the {@link #searchPromptPlaceholderText(Class)}.
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    default TranslationContext translationContext(
            final @NonNull Class<?> domainType) {
        return TranslationContext.named("Search");
    }

}