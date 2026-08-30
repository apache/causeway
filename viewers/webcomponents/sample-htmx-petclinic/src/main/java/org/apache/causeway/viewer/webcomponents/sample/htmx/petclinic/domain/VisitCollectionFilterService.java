/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.filter.CollectionFilterService;
import org.apache.causeway.applib.services.i18n.TranslatableString;

@Service
public class VisitCollectionFilterService implements CollectionFilterService {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public boolean handles(final Class<?> domainType) {
        return Visit.class.isAssignableFrom(domainType);
    }

    @Override
    public <T> Function<T, Tokens> tokenizer(final Class<T> domainType) {
        return candidate -> {
            var visit = (Visit) candidate;
            var searchable = String.join(" ",
                    visit.getPet().getName(),
                    visit.getVisitAt().toString(),
                    visit.getVisitAt().format(DISPLAY_DATE),
                    visit.getReason(),
                    visit.getNotes() == null ? "" : visit.getNotes())
                    .toLowerCase(Locale.ROOT);
            return searchArgument -> Arrays.stream(searchArgument.toLowerCase(Locale.ROOT).split("\\s+"))
                    .allMatch(searchable::contains);
        };
    }

    @Override
    public TranslatableString searchPromptPlaceholderText(final Class<?> domainType) {
        return TranslatableString.tr("Search visits");
    }
}
