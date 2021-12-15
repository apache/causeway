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
package org.apache.isis.core.runtimeservices.locale;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.locale.LocaleChoiceProvider;
import org.apache.isis.commons.collections.Can;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@Named("isis.runtimeservices.LocaleChoiceProviderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LocaleChoiceProviderDefault
implements LocaleChoiceProvider {

    @Override
    public List<Locale> getAvailableLocales() {
        return getSupportedLanguages().toList();
    }

    // -- HELPER

    @Getter(value = AccessLevel.PROTECTED, lazy = true)
    private final Can<Locale> supportedLanguages = streamSupportedLanguages()
        .collect(Can.toCan());

    /**
     * Stream subset of {@link Locale#getISOLanguages()} that supports round-tripping.
     */
    private Stream<Locale> streamSupportedLanguages() {
        return Stream.of(Locale.getISOLanguages())
                .sorted()
                .map(this::localeForIsoLanguage)
                .filter(this::isRoundtripSupported);
    }

    private boolean isRoundtripSupported(final Locale locale) {
        return locale.equals(Locale.forLanguageTag(locale.toLanguageTag()));
    }

    private Locale localeForIsoLanguage(final String isoLanguage) {
        return new Locale.Builder().setLanguage(isoLanguage).build();
    }

}
