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

import java.util.EnumSet;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.collections.ImmutableEnumSet;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Immutable value object that holds literals for its various supported {@link NounForm}(s).
 * @since 2.0
 */
@Value @Builder
public class NounForms {

    public static NounFormsBuilder builderSingular(final @Nullable String singular) {
        return NounForms.builder()
                .singular(singular);
    }

    private final @Nullable String singular;

    public boolean isNounPresent() {
        return singular!=null;
    }

    public NounForms translate(
            final @NonNull TranslationService translationService,
            final TranslationContext context) {

        val builder = NounForms
                .builder();

        if(isNounPresent()) {
            builder.singular(translationService.translate(context, singular));
        }
        return builder.build();
    }

    public ImmutableEnumSet<NounForm> getSupportedNounForms() {

        val enumSet = singular!=null
                ? EnumSet.of(NounForm.SINGULAR)
                : EnumSet.noneOf(NounForm.class);

        return ImmutableEnumSet.from(enumSet);
    }

    public Optional<String> lookup() {
        return isNounPresent()
                ? Optional.of(getSingular())
                : Optional.empty();
    }


}
