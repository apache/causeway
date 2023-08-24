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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;

import lombok.NonNull;

/**
 * Immutable value object that holds the null-able literal (String) for a noun.
 *
 * @since 2.0
 */
@lombok.Value(staticConstructor = "singular")
public class Noun {

    private final @Nullable String singular;

    public boolean isLiteralPresent() {
        return singular!=null;
    }

    public Optional<String> literal() {
        return isLiteralPresent()
                ? Optional.of(getSingular())
                : Optional.empty();
    }

    public Noun translate(
            final @NonNull TranslationService translationService,
            final TranslationContext context) {

        return isLiteralPresent()
                ? singular(translationService.translate(context, singular))
                : this;
    }

}
