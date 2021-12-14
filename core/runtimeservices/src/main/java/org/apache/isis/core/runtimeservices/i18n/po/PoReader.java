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
package org.apache.isis.core.runtimeservices.i18n.po;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.i18n.LanguageProvider;
import org.apache.isis.applib.services.i18n.Mode;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.extern.log4j.Log4j2;

@Log4j2
class PoReader extends PoAbstract {

    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";

    private final Map<Locale, Map<ContextAndMsgId, String>> translationByKeyByLocale = _Maps.newHashMap();
    private final Map<Locale, Boolean> usesFallbackByLocale = _Maps.newHashMap();

    /**
     * The basename of the translations file, hard-coded to <tt>translations</tt>.
     *
     * <p>
     *     This means that the reader will search for <tt>translations_en-US.po</tt>, <tt>translations_en.po</tt>,
     *     <tt>translations.po</tt>, according to the location that the provided
     *     {@link org.apache.isis.applib.services.i18n.TranslationsResolver} searches.
     * </p>
     *
     * <p>
     *     For example, if using the Wicket implementation, then will search for these files
     *     under <tt>/WEB-INF</tt> directory.
     * </p>
     */
    private final String basename = "translations";
    private final Can<TranslationsResolver> translationsResolver;
    private final LanguageProvider languageProvider;

    private List<String> fallback;

    public PoReader(final TranslationServicePo translationServicePo) {
        super(translationServicePo, Mode.READ);
        translationsResolver = translationServicePo.getTranslationsResolver();
        if(translationsResolver.isEmpty()) {
            log.warn("No TranslationsResolver available");
        }
        languageProvider = translationServicePo.getLanguageProvider();
    }

    // -- init, shutdown

    /**
     * Not API
     */
    void init() {
        fallback = readUrl(basename + ".po");
        if(fallback == null) {
            log.info("No fallback translations found; i18n is in effect disabled for this application");
            fallback = Collections.emptyList();
        }
    }

    @Override
    public String translate(final TranslationContext context, final String msgId) {
        if(translationsResolver == null) {
            // already logged as WARN (in constructor) if null.
            return msgId;
        }
        return translate(context, msgId, ContextAndMsgId.Type.REGULAR);
    }

    @Override
    String translate(final TranslationContext context, final String msgId, final String msgIdPlural, final int num) {

        final String msgIdToUse;
        final ContextAndMsgId.Type type;
        if (num == 1) {
            msgIdToUse = msgId;
            type = ContextAndMsgId.Type.REGULAR;
        } else {
            msgIdToUse = msgIdPlural;
            type = ContextAndMsgId.Type.PLURAL_ONLY;
        }

        return translate(context, msgIdToUse, type);
    }

    void clearCache() {
        translationByKeyByLocale.clear();
        usesFallbackByLocale.clear();
        init();
    }

    private String translate(final TranslationContext context, final String msgId, final ContextAndMsgId.Type type) {

        final Locale targetLocale;
        try {
            targetLocale = languageProvider.getPreferredLanguage()
                    .orElse(null);
            if(targetLocale == null) {
                // eg if request from RO viewer and the (default) LocaleProviderWicket is being used.
                return msgId;
            }
        } catch(final RuntimeException ex){
            logInfoIfNotPreviously("Failed to obtain locale, returning the original msgId");
            return msgId;
        }


        final Map<ContextAndMsgId, String> translationsByKey = readAndCacheTranslationsIfRequired(targetLocale);

        // search for translation with a context
        final ContextAndMsgId key = new ContextAndMsgId(context.getName(), msgId, type);
        final String translation = lookupTranslation(translationsByKey, key);
        if (!_Strings.isNullOrEmpty(translation)) {
            return translation;
        }

        // else search for translation without a context
        final ContextAndMsgId keyNoContext = new ContextAndMsgId("", msgId, type);
        final String translationNoContext = lookupTranslation(translationsByKey, keyNoContext);
        if (!_Strings.isNullOrEmpty(translationNoContext)) {
            return translationNoContext;
        }

        // to avoid chattiness in the log, we only log if there are ANY translations at all for the target locale.
        // the algorithm for searching for translations looks for:
        // 1. language_country
        // 2. language
        // 3. fallback
        // so this message is only ever displayed if the locale isn't using fallback (ie a translation is genuinely missing)
        final Boolean usesFallback = usesFallbackByLocale.get(targetLocale);
        if(usesFallback == null || !usesFallback) {
            logInfoIfNotPreviously("No translation found for: " + key);
        }

        return msgId;
    }


    private String lookupTranslation(final Map<ContextAndMsgId, String> translationsByKey, final ContextAndMsgId key) {
        final String s = translationsByKey.get(key);
        return s != null? s.trim(): null;
    }

    private Map<ContextAndMsgId, String> readAndCacheTranslationsIfRequired(final Locale locale) {
        Map<ContextAndMsgId, String> translationsByKey = translationByKeyByLocale.get(locale);
        if(translationsByKey != null) {
            return translationsByKey;
        }

        translationsByKey = _Maps.newHashMap();
        read(locale, translationsByKey);
        translationByKeyByLocale.put(locale, translationsByKey);

        return translationsByKey;
    }


    /**
     * @param locale - the .po file to load
     * @param translationsByKey - the translations to be populated
     */
    private void read(final Locale locale, final Map<ContextAndMsgId, String> translationsByKey) {
        final List<String> contents = readPo(locale);

        Block block = new Block();
        for (final String line : contents) {
            block = block.parseLine(line, translationsByKey);
        }
    }

    protected List<String> readPo(final Locale locale) {
        final List<String> lines = readPoElseNull(locale);
        if(lines != null) {
            usesFallbackByLocale.put(locale, false);
            return lines;
        }

        // this is only ever logged the first time that a user using this particular locale is encountered
        logInfoIfNotPreviously("Could not locate translations for locale: " + locale + ", using fallback");

        usesFallbackByLocale.put(locale, true);
        return fallback;
    }

    private List<String> readPoElseNull(final Locale locale) {
        final String country = locale.getCountry().toUpperCase(Locale.ROOT);
        final String language = locale.getLanguage().toLowerCase(Locale.ROOT);

        final List<String> candidates = _Lists.newArrayList();
        if(!_Strings.isNullOrEmpty(language)) {
            if(!_Strings.isNullOrEmpty(country)) {
                candidates.add(basename + DASH       + language + UNDERSCORE + country+ ".po");
                candidates.add(basename + DASH       + language + DASH       + country+ ".po");
                candidates.add(basename + UNDERSCORE + language + UNDERSCORE + country+ ".po");
                candidates.add(basename + UNDERSCORE + language + DASH       + country+ ".po");
            }
            candidates.add(basename + DASH + language + ".po");
            candidates.add(basename + UNDERSCORE + language + ".po");
        }

        for (final String candidate : candidates) {
            final List<String> lines = readUrl(candidate);
            if(lines != null) {
                return lines;
            }
        }
        return null;
    }

    private List<String> readUrl(final String candidate) {
        return translationsResolver.stream()
                .map(resolver->resolver.readLines(candidate))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    // to avoid flooding the logs
    private void logInfoIfNotPreviously(final String infoMessage) {
        if(!loggedInfoMessages.contains(infoMessage)) {
            log.info(infoMessage);
            loggedInfoMessages.add(infoMessage);
        }
    }

    private final Set<String> loggedInfoMessages = _Sets.newConcurrentHashSet();


}
