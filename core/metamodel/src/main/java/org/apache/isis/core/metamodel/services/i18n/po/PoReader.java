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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PoReader extends PoAbstract {

    public static Logger LOG = LoggerFactory.getLogger(PoReader.class);

    private final Map<Locale, Map<MsgIdAndContext, String>> translationByKeyByLocale = Maps.newHashMap();

    public PoReader(final TranslationServicePo translationServicePo) {
        super(translationServicePo);
    }

    //region > init, shutdown
    void init(final Map<String,String> config) {
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
        final List<String> fileContents = readFile(locale);

        // TODO: parse fileContents into translationsByKey
    }

    List<String> readFile(final Locale locale) {
        final File file = locateFile(locale);
        try {
            return Files.readLines(file, Charsets.UTF_8);
        } catch (final IOException ex) {
            LOG.warn("Could not locate file for locale: " + locale, ex);
            return Collections.emptyList();
        }
    }

    File locateFile(final Locale locale) {
        return null;
    }

    private String translate(final Locale locale, final MsgIdAndContext key) {
        // TODO
        return key.getMsgId();
    }


}
