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
package org.apache.causeway.core.metamodel.tabular.interactive;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.search.CollectionSearchService;
import org.apache.causeway.applib.services.search.CollectionSearchService.Tokens;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _SearchUtils {

    @AllArgsConstructor
    static class SearchHandler {

        @NonNull final Function<Object, Tokens> tokenizer;
        @NonNull final BiPredicate<Tokens, String> matcher;
        @NonNull final String searchPromptPlaceholderText;

        /**
         * @deprecated {@link Tokens} could be stored with the {@link DataRow}
         *      on first request-cycle, such we don't need to re-hydrate pojos
         *      on follow-up partial page updates
         */
        @Deprecated
        @NonNull final BiPredicate<Object, String> searchPredicate() {
            return (pojo, searchArg) -> {
                var tokens = tokenizer.apply(pojo);
                return matcher.test(tokens, searchArg);
            };
        }
    }

    Optional<SearchHandler> createSearchHandler(final @NonNull ObjectSpecification elementType) {
        var mmc = elementType.getMetaModelContext();
        var collectionSearchServiceOpt = mmc.getServiceRegistry().select(CollectionSearchService.class).stream()
                .filter(service->service.handles(elementType.getCorrespondingClass()))
                .findFirst();
        if(!collectionSearchServiceOpt.isPresent()) {
            return Optional.empty();
        }
        var collectionSearchService = collectionSearchServiceOpt.get();

        var tokenizer =
                collectionSearchService.tokenizer(elementType.getCorrespondingClass());
        if(tokenizer==null) {
            return Optional.empty();
        }

        var matcher = collectionSearchService.matcher(elementType.getCorrespondingClass());
        if(matcher==null) {
            return Optional.empty();
        }

        var translationService = mmc.lookupService(TranslationService.class)
                .orElseGet(TranslationService::identity);
        var translationContext = Optional.ofNullable(collectionSearchService.translationContext(elementType.getCorrespondingClass()))
                .orElseGet(()->TranslationContext.named("Search"));
        var translatableString = collectionSearchService.searchPromptPlaceholderText(elementType.getCorrespondingClass());

        var searchPromptPlaceholderText = translatableString!=null
                ? translatableString.translate(translationService, translationContext)
                : "";

        return Optional.of(new SearchHandler(
                _Casts.uncheckedCast(tokenizer),
                matcher,
                searchPromptPlaceholderText));
    }

}