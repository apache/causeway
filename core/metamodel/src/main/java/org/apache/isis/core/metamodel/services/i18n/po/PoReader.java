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

class PoReader extends PoAbstract {

    public static final String LOCATION_BASE_URL = "isis.services.translation.po.locationBaseUrl";
    public static Logger LOG = LoggerFactory.getLogger(PoReader.class);

    private final Map<Locale, Map<MsgIdAndContext, String>> translationByKeyByLocale = Maps.newHashMap();

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
        super(translationServicePo);
    }

    //region > init, shutdown
    void init(final Map<String,String> config) {
        fallback = readPoElseNull(Locale.ROOT);
        if(fallback == null) {
            LOG.warn("No fallback translations found");
            fallback = Collections.emptyList();
        }
    }

    void shutdown() {
    }
    //endregion

    public String translate(final String context, final String msgId, final Locale targetLocale) {

        final Map<MsgIdAndContext, String> translationsByKey = readAndCacheTranslationsIfRequired(targetLocale);

        final MsgIdAndContext key = new MsgIdAndContext(msgId, context);
        final String translation = translationsByKey.get(key);
        if (translation != null) {
            return translation;
        }

        final MsgIdAndContext keyNoContext = new MsgIdAndContext(msgId, "");
        final String translationNoContext = translationsByKey.get(keyNoContext);
        if (translationNoContext != null) {
            return translationNoContext;
        }

        LOG.warn("No translation found for: " + key);
        return msgId;
    }

    private Map<MsgIdAndContext, String> readAndCacheTranslationsIfRequired(final Locale locale) {
        Map<MsgIdAndContext, String> translationsByKey = translationByKeyByLocale.get(locale);
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
    private void read(final Locale locale, final Map<MsgIdAndContext, String> translationsByKey) {
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
        return translationServicePo.urlResolver.readLines(candidate);
    }

}
