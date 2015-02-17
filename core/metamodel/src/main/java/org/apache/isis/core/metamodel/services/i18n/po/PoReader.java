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
package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.UrlResolver;

class PoReader extends PoAbstract {

    public static final String LOCATION_BASE_URL = "isis.services.translation.po.locationBaseUrl";
    public static Logger LOG = LoggerFactory.getLogger(PoReader.class);

    private final Map<Locale, Map<ContextAndMsgId, String>> translationByKeyByLocale = Maps.newHashMap();

    /**
     * The basename of the translations file, hard-coded to <tt>translations</tt>.
     *
     * <p>
     *     This means that the reader will search for <tt>translations_en-US.po</tt>, <tt>translations_en.po</tt>,
     *     <tt>translations.po</tt>, according to the location that the provided {@link org.apache.isis.applib.services.i18n.UrlResolver} searches.  For example, if using the Wicket implementation, then will search for these files
     *     under <tt>/WEB-INF</tt> directory.
     * </p>
     */
    private final String basename = "translations";

    private List<String> fallback;

    public PoReader(final TranslationServicePo translationServicePo) {
        super(translationServicePo, TranslationService.Mode.READ);
    }

    //region > init, shutdown
    void init(final Map<String,String> config) {
        fallback = readUrl(basename + ".po");
        if(fallback == null) {
            LOG.warn("No fallback translations found");
            fallback = Collections.emptyList();
        }
    }

    @Override
    void shutdown() {
    }
    //endregion

    public String translate(final String context, final String msgId) {
        final Locale locale = translationServicePo.getLocaleProvider().getLocale();
        return translate(context, msgId, ContextAndMsgId.Type.REGULAR, locale);
    }

    @Override
    String translate(final String context, final String msgId, final String msgIdPlural, final int num) {

        final Locale locale = translationServicePo.getLocaleProvider().getLocale();
        final String msgIdToUse;
        final ContextAndMsgId.Type type;
        if (num == 1) {
            msgIdToUse = msgId;
            type = ContextAndMsgId.Type.REGULAR;
        } else {
            msgIdToUse = msgIdPlural;
            type = ContextAndMsgId.Type.PLURAL_ONLY;
        }

        return translate(context, msgIdToUse, type, locale);
    }

    private String translate(
            final String context, final String msgId, final ContextAndMsgId.Type type,
            final Locale targetLocale) {
        final Map<ContextAndMsgId, String> translationsByKey = readAndCacheTranslationsIfRequired(targetLocale);

        final ContextAndMsgId key = new ContextAndMsgId(context, msgId, type);
        final String translation = lookupTranslation(translationsByKey, key);
        if (!Strings.isNullOrEmpty(translation)) {
            return translation;
        }

        final ContextAndMsgId keyNoContext = new ContextAndMsgId("", msgId, type);
        final String translationNoContext = lookupTranslation(translationsByKey, keyNoContext);
        if (!Strings.isNullOrEmpty(translationNoContext)) {
            return translationNoContext;
        }

        LOG.warn("No translation found for: " + key);
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

        translationsByKey = Maps.newHashMap();
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
            return lines;
        }
        LOG.warn("Could not locate translations for locale: " + locale + ", using fallback");
        return fallback;
    }

    private List<String> readPoElseNull(final Locale locale) {
        final String country = locale.getCountry().toUpperCase(Locale.ROOT);
        final String language = locale.getLanguage().toLowerCase(Locale.ROOT);

        final List<String> candidates = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(language)) {
            if(!Strings.isNullOrEmpty(country)) {
                candidates.add(basename + "_" + language + "-" + country+ ".po");
            }
            candidates.add(basename + "_" + language + ".po");
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
        final UrlResolver urlResolver = translationServicePo.getUrlResolver();
        if(urlResolver == null) {
            return null;
        }
        return urlResolver.readLines(candidate);
    }

}
