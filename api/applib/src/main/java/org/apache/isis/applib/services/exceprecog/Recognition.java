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
package org.apache.isis.applib.services.exceprecog;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Represents a user-friendly representation of an exception that has been
 * recognised by an available implementation of an {@link ExceptionRecognizer}.
 *
 * <p>
 * Returned by {@link ExceptionRecognizer#recognize(Throwable)} when the
 * exception recognizer has recognised the exception
 *
 * @since 1.x {@index}
 */
@Value
public class Recognition {

    /**
     * @return optionally a recognition of the specified type, based on a whether given reason is non-null
     */
    public static Optional<Recognition> of(
            @Nullable final Category category,
            @Nullable final String reason) {

        if (reason == null) {
            return Optional.empty();
        }

        val nonNullCategory = category != null ? category : Category.OTHER;
        return Optional.of(new Recognition(nonNullCategory, reason));
        // ...
    }

    /**
     * Categorises the exception as per {@link Category}.
     *
     * <p>
     * In essence, if an exception is recognized then it is also categorized.
     * This lets the viewer act accordingly.
     *
     * If the implementation recognizes the exception then it returns a user-friendly message to be rendered (by the viewer) back to the user; otherwise it returns `null`.
     * There is no need for the implementation to check for exception causes; the casual chain is unwrapped by Apache Isis core and each exception in the chain will also be passed through to the recognizer (from most specific to least).
     * The recognizer implementation can therefore be as fine-grained or as coarse-grained as it wishes.
     * </p>
     * <p>
     *     This category can also optionally be used in the translation of the
     *     {@link #getReason() reason} for the exception.
     * </p>
     *
     * @see #toMessage(TranslationService)
     */
    @NonNull
    Category category;

    /**
     * The untranslated user-friendly reason for the exception.
     *
     * <p>
     *     The reason can also be translated (prepended or not by the
     *     translation of the {@link #getCategory() category} using
     *     {@link #toMessage(TranslationService)} or
     *     {@link #toMessageNoCategory(TranslationService)}.
     *     .
     * </p>
     *
     * @see #toMessage(TranslationService)
     * @see #toMessageNoCategory(TranslationService)
     */
    @NonNull
    String reason;

    /**
     * Translates the {@link #getReason() reason} and prepends with a
     * translation of {@link #getCategory() category}, using the provided
     * {@link TranslationService}..
     *
     * @param translationService
     */
    public String toMessage(@Nullable TranslationService translationService) {

        val categoryLiteral = translate(getCategory().getFriendlyName(), translationService);
        val reasonLiteral = translate(getReason(), translationService);

        return String.format("[%s]: %s", categoryLiteral, reasonLiteral);
        // ...
    }

    /**
     * Translates the {@link #getReason() reason} alone (ignoring the
     * {@link #getCategory() category}, using the provided
     * {@link TranslationService}..
     *
     * @param translationService
     */
    public String toMessageNoCategory(@Nullable TranslationService translationService) {

        val reasonLiteral = translate(getReason(), translationService);
        return String.format("%s", reasonLiteral);
        // ...
    }

    private static String translate(
            @Nullable String x,
            @Nullable TranslationService translationService) {
        if (x == null || translationService == null) {
            return x;
        }
        TranslationContext context = TranslationContext.ofClass(Recognition.class);
        return translationService.translate(
        		context, x);
    }

}
