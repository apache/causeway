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
package org.apache.causeway.core.metamodel.facets.all.i8n.noun;

import java.util.Optional;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Immutable value object that holds the null-able literal (String) for a noun.
 * For the Noun to be non-empty requires the literal to contain at least one non-whitespace character.
 *
 * @since 2.0
 * @see StringUtils#hasText
 */
@EqualsAndHashCode
public final class Noun {

	@Getter @Accessors(fluent = true)
	private final @Nullable String nullableLiteral;

    // canonical constructor
    public Noun(
        @Nullable final String nullableLiteral) {
        this.nullableLiteral = StringUtils.hasText(nullableLiteral)
            ? nullableLiteral
            : null;
    }

    public boolean isEmpty() { return nullableLiteral==null; }
    public boolean isPresent() { return nullableLiteral!=null; }

    public Optional<String> literal() {
        return isPresent()
                ? Optional.of(nullableLiteral)
                : Optional.empty();
    }

    public Noun translate(
            final @NonNull TranslationService translationService,
            final TranslationContext context) {
        return isPresent()
                ? new Noun(translationService.translate(context, nullableLiteral))
                : this;
    }

}
