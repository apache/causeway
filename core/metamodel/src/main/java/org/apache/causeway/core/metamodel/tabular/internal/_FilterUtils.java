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
package org.apache.causeway.core.metamodel.tabular.internal;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.causeway.applib.services.filter.CollectionFilterService;
import org.apache.causeway.applib.services.filter.CollectionFilterService.Tokens;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.tabular.DataRow;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _FilterUtils {

    @AllArgsConstructor
    static class FilterHandler {

        @NonNull final Function<Object, Tokens> tokenizer;
        @NonNull final BiPredicate<Tokens, String> tokenFilter;
        @NonNull final String searchPromptPlaceholderText;

        @NonNull final BiPredicate<DataRow, String> getDataRowFilter() {
            return (dataRow, searchArg) ->
                tokenFilter.test(dataRow.getFilterTokens().orElse(null), searchArg);
        }
    }

    Optional<FilterHandler> createFilterHandler(final @NonNull ObjectSpecification elementType) {
        var mmc = elementType.getMetaModelContext();
        var collectionFilterServiceOpt = mmc.getServiceRegistry().select(CollectionFilterService.class).stream()
                .filter(service->service.handles(elementType.getCorrespondingClass()))
                .findFirst();
        if(!collectionFilterServiceOpt.isPresent()) {
            return Optional.empty();
        }
        var collectionFilterService = collectionFilterServiceOpt.get();

        var tokenizer =
                collectionFilterService.tokenizer(elementType.getCorrespondingClass());
        if(tokenizer==null) {
            return Optional.empty();
        }

        var tokenFilter = collectionFilterService.tokenFilter(elementType.getCorrespondingClass());
        if(tokenFilter==null) {
            return Optional.empty();
        }

        var translationService = mmc.lookupService(TranslationService.class)
                .orElseGet(TranslationService::identity);
        var translationContext = Optional.ofNullable(collectionFilterService.translationContext(elementType.getCorrespondingClass()))
                .orElseGet(()->TranslationContext.named("Search"));
        var translatableString = collectionFilterService.searchPromptPlaceholderText(elementType.getCorrespondingClass());

        var searchPromptPlaceholderText = translatableString!=null
                ? translatableString.translate(translationService, translationContext)
                : "";

        return Optional.of(new FilterHandler(
                _Casts.uncheckedCast(tokenizer),
                tokenFilter,
                searchPromptPlaceholderText));
    }

}
