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
package org.apache.isis.core.metamodel.facets.all.i8n;

import java.util.EnumSet;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public class NounForms {

    public static NounFormsBuilder preferredSingular() {
        return NounForms.builder()
                .preferredNounForm(NounForm.SINGULAR);
    }

    public static NounFormsBuilder preferredPlural() {
        return NounForms.builder()
                .preferredNounForm(NounForm.PLURAL);
    }

    private final @Nullable String indifferent;
    private final @Nullable String empty;
    private final @Nullable String singular;
    private final @Nullable String plural;

    private final @NonNull NounForm preferredNounForm;

    @Getter(lazy = true)
    final EnumSet<NounForm> supportedNounForms = supportedNounForms();

    private EnumSet<NounForm> supportedNounForms() {

        val supportedNounForms = EnumSet.noneOf(NounForm.class);

        if(indifferent!=null) {
            supportedNounForms.add(NounForm.INDIFFERENT);
        }

        if(empty!=null) {
            supportedNounForms.add(NounForm.EMPTY);
        }

        if(singular!=null) {
            supportedNounForms.add(NounForm.SINGULAR);
        }

        if(plural!=null) {
            supportedNounForms.add(NounForm.PLURAL);
        }

        return supportedNounForms;
    }

    public String get(final @NonNull NounForm nounForm) {
        if(!getSupportedNounForms().contains(nounForm)) {
            throw _Exceptions.illegalArgument("NounForm %s not supported with this instance", nounForm);
        };
        switch(nounForm) {
        case INDIFFERENT:
            return getIndifferent();
        case EMPTY:
            return getEmpty();
        case SINGULAR:
            return getSingular();
        case PLURAL:
            return getPlural();
        default:
            break;
        }
        throw _Exceptions.unmatchedCase(nounForm);
    }

    public NounForms translate(
            final @NonNull TranslationService translationService,
            final TranslationContext context) {

        val builder = NounForms
                .builder()
                .preferredNounForm(preferredNounForm);


        getSupportedNounForms()
        .forEach(nounForm->{

            switch(nounForm) {
            case INDIFFERENT:
                builder.indifferent(translationService.translate(context, indifferent));
                break;
            case EMPTY:
                builder.empty(translationService.translate(context, empty));
                break;
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
