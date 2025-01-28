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
package org.apache.causeway.applib.locale;

import java.io.Serializable;
import java.util.Locale;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import lombok.Value;

/**
 * User specific regional preferred settings.
 *
 * @apiNote thread-safe and serializable
 * @since 2.0 {@index}
 */
@org.apache.causeway.applib.annotation.Value
@Value
@Builder
public class UserLocale
implements Serializable {

    private static final long serialVersionUID = 1L;

    public static UserLocale getDefault() {
        return valueOf(Locale.getDefault());
    }

    public static UserLocale valueOf(final Locale mainLocale) {
        return UserLocale.builder()
                .languageLocale(mainLocale)
                .numberFormatLocale(mainLocale)
                .timeFormatLocale(mainLocale)
                .build();
    }

    private final @NonNull Locale languageLocale;
    private final @NonNull Locale numberFormatLocale;
    private final @NonNull Locale timeFormatLocale;

    // -- UTILITY

    public UserLocaleBuilder asBuilder() {
        return UserLocale.builder()
                .languageLocale(languageLocale)
                .numberFormatLocale(numberFormatLocale)
                .timeFormatLocale(timeFormatLocale);
    }

}
