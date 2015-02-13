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

import java.util.Locale;
import java.util.Map;
import com.google.common.collect.Maps;
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

        Map<MsgIdAndContext, String> translationByKey = translationByKeyByLocale.get(targetLocale);
        if(translationByKey == null) {
            translationByKey = Maps.newTreeMap();
            translationByKeyByLocale.put(targetLocale, translationByKey);
        }

        final MsgIdAndContext key = new MsgIdAndContext(msgId, context);
        String translation = translationByKey.get(key);
        if (translation == null) {
            translation = translate(targetLocale, key);
            translationByKey.put(key, translation);
        }

        return translation;
    }

    private String translate(final Locale locale, final MsgIdAndContext key) {
        // TODO
        return key.getMsgId();
    }


}
