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
package org.apache.isis.core.metamodel.facets.all.i8n.noun;

import java.util.EnumSet;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Immutable value object that holds literals for its various supported {@link NounForm}(s).
 * @since 2.0
 */
@Value @Builder
public class NounForms {

    public static NounFormsBuilder builderSingular(@Nullable final String singular) {
        return NounForms.builder()
                .singular(singular);
    }

    public static NounFormsBuilder builderPlural(@Nullable final String plural) {
        return NounForms.builder()
                .plural(plural);
    }

    private final @Nullable String singular;
    private final @Nullable String plural;

    @Getter(lazy = true)
    final ImmutableEnumSet<NounForm> supportedNounForms = supportedNounForms();

    private ImmutableEnumSet<NounForm> supportedNounForms() {

        val supportedNounForms = EnumSet.noneOf(NounForm.class);

        if(singular!=null) {
            supportedNounForms.add(NounForm.SINGULAR);
        }

        if(plural!=null) {
            supportedNounForms.add(NounForm.PLURAL);
        }

        return ImmutableEnumSet.from(supportedNounForms);
    }

    public Optional<String> lookup(final @NonNull NounForm nounForm) {
        if(!getSupportedNounForms().contains(nounForm)) {
            return Optional.empty();
        };
        switch(nounForm) {
        case SINGULAR:
            // non-null, as nulls are guarded by getSupportedNounForms()
            return Optional.of(getSingular());
        case PLURAL:
            // non-null, as nulls are guarded by getSupportedNounForms()
            return Optional.of(getPlural());
        default:
            break;
        }
        throw _Exceptions.unmatchedCase(nounForm);
    }

    public NounForms translate(
            final @NonNull TranslationService translationService,
            final TranslationContext context) {

        val builder = NounForms
                .builder();

        getSupportedNounForms()
        .forEach(nounForm->{

            switch(nounForm) {
            case SINGULAR:
                builder.singular(translationService.translate(context, singular));
                break;
            case PLURAL:
                builder.plural(translationService.translate(context, plural));
                break;
            default:
                throw _Exceptions.unmatchedCase(nounForm);
            }

        });

        return builder.build();
    }

}
