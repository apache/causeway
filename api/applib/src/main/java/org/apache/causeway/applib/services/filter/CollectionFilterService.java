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
package org.apache.causeway.applib.services.filter;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;

/**
 * EXPERIMENTAL/DRAFT
 * <p>
 * If at least one {@link CollectionFilterService} is registered with Spring's context
 * that handles a given domainType,
 * viewer implementations (like Wicket Viewer) should show
 * a quick-search prompt, which is rendered on top of the UI table that
 * presents the collection in question.
 *
 * @since 2.1, 3.1 {@index}
 */
public interface CollectionFilterService {

    public static interface Tokens extends Serializable {
        /**
         * @apiNote for convenience, the searchArg can be pre-processed by
         *      by the {@link CollectionFilterService#tokenFilter(Class)};
         *      The default implementation already handles the empty searchArg case.
         */
        boolean match(@Nullable String searchArg);
    }

    /**
     * Whether this service handles given type.
     * @param domainType - entity or view-model type to be rendered as row in a table
     */
    boolean handles(@NonNull Class<?> domainType);

    // -- TOKENIZER

    /**
     * Returns a function that for a given domainObject returns {@link Tokens} (words),
     * that are then matchable by {@link #tokenFilter(Class)}.
     * <p>
     * For example the domain object's title could be tokenized (parsed into tokens).
     *
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    <T> Function<T, Tokens> tokenizer(
            @NonNull Class<T> domainType);

    // -- MATCHER

    /**
     * For given {@code domainType} returns a {@link BiPredicate}
     * that matches {@link Tokens} against a nullable searchString.
     * <p>
     * For example, the searchString could be tokenized (parsed into tokens),
     * and then matched against the {@link Tokens} using either OR or AND semantics.
     * <p>
     * The default implementation sanitizes the user input (searchArg)
     * then strips any leading or trailing blanks and handles the empty searchArg case.
     *
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    default <T> BiPredicate<Tokens, String> tokenFilter(
            final @NonNull Class<T> domainType) {
        return (tokens, searchArg) -> {
            var sanitized = _Strings.blankToNullOrTrim(searchArg);
            return _Strings.isNotEmpty(sanitized)
                ? tokens!=null
                    ? tokens.match(sanitized)
                    : false // if an element has no tokens, it cannot be found by any search
                : true; // empty searchArg means 'unfiltered'
        };
    }

    // -- SEARCH ARG SANITIZER

    /**
     * Pre-process user input before passing it to the matcher.
     * <p>
     * Default behavior is to strip leading or trailing blanks (if any).
     */
    default String sanitzeSearchArgument(final @Nullable String searchArg) {
        return _Strings.blankToNullOrTrim(searchArg);
    }

    // -- SEARCH PROMT

    /**
     * Placeholder text for the quick-search prompt.
     * @param domainType - entity or view-model type to be rendered as row in a table
     * @apiNote guarded by a call to {@link #handles(Class)}
     */
    default TranslatableString searchPromptPlaceholderText(
            final @NonNull Class<?> domainType) {
        return TranslatableString.tr("Search {domainType}", "domainType",
                _Strings.asNaturalName.andThen(_Strings.asLowerCase)
                    .apply(domainType.getSimpleName()));
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