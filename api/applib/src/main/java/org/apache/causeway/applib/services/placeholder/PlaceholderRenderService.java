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
package org.apache.causeway.applib.services.placeholder;

import java.util.Map;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._StringInterpolation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Service that renders various {@link PlaceholderLiteral}s, as presented with the UI.
 *
 * @since 2.0 {@index}
 */
public interface PlaceholderRenderService {

    @Getter
    @RequiredArgsConstructor
    public static enum PlaceholderLiteral {
        NULL_REPRESENTATION("none"),
        SUPPRESSED("suppressed"),
        HAS_MORE("has ${number} more");
        ;
        private final String literal;
    }

    /**
     * Textual representation of given {@link PlaceholderLiteral},
     * as used for eg. titles and choice drop-downs.
     */
    String asText(@NonNull PlaceholderLiteral placeholderLiteral, @Nullable Map<String, String> vars);
    default String asText(@NonNull final PlaceholderLiteral placeholderLiteral) {
        return asText(placeholderLiteral, null);
    }

    /**
     * HTML representation of given {@link PlaceholderLiteral},
     * as used for rendering with the UI (when appropriate).
     */
    String asHtml(@NonNull PlaceholderLiteral placeholderLiteral, @Nullable Map<String, String> vars);
    default String asHtml(@NonNull final PlaceholderLiteral placeholderLiteral) {
        return asHtml(placeholderLiteral, null);
    }

    static String interpolate(final String raw, @Nullable final Map<String, String> vars) {
        return vars!=null
                ? new _StringInterpolation(vars).applyTo(raw)
                : raw;
    }

    static PlaceholderRenderService fallback() {
        return new PlaceholderRenderService() {
            @Override public String asText(
                    @NonNull final PlaceholderLiteral placeholderLiteral,
                    @Nullable final Map<String, String> vars) {
                return PlaceholderRenderService.interpolate(placeholderLiteral.getLiteral(), vars);
            }

            @Override public String asHtml(
                    @NonNull final PlaceholderLiteral placeholderLiteral,
                    @Nullable final Map<String, String> vars) {
                return asText(placeholderLiteral, vars);
            }
        };
    }

}
