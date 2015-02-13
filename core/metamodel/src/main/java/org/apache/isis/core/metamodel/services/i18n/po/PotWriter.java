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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import com.google.common.collect.TreeMultimap;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PotWriter extends PoAbstract {

    public static Logger LOG = LoggerFactory.getLogger(PotWriter.class);

    private final TreeMultimap<String, String> contextsByMsgId = TreeMultimap.create();

    public PotWriter(final TranslationServicePo translationServicePo) {
        super(translationServicePo);
    }


    //region > init, shutdown
    void init(final Map<String,String> config) {
    }

    void shutdown() {
        LOG.info("");
        LOG.info("");
        LOG.info("################################################################################");
        LOG.info("#");
        LOG.info("# " + LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        LOG.info("#");
        LOG.info("################################################################################");
        LOG.info("");
        LOG.info("");
        LOG.info("");
        LOG.info(toPo());
        LOG.info("");
        LOG.info("");
        LOG.info("");
    }
    //endregion


    public String translate(final String context, final String originalText, final Locale targetLocale) {
        final NavigableSet<String> contexts = contextsByMsgId.get(originalText);
        contexts.add(context);
        return originalText;
    }

    /**
     * Not API
     */
    String toPo() {
        final Map<String, Collection<String>> messages = messagesWithContext();
        final StringBuilder buf = new StringBuilder();
        for (String message : messages.keySet()) {
            final Collection<String> contexts = messages.get(message);
            for (String context : contexts) {
                buf.append("#: ").append(context).append("\n");
            }
            buf.append("msgid: \"").append(message).append("\"\n");
            buf.append("msgstr: \"\"\n");
            buf.append("\n\n\n");
        }
        return buf.toString();
    }

    /**
     * Returns the set of messages encountered and cached by the service (the key of the map) along with a set of
     * context strings (the value of the map)
     *
     * <p>
     *     The intention is that an implementation running in prototype mode should retain all requests to
     *     {@link #translate(String, String, java.util.Locale)}, such that they can be translated and used by the
     *     same implementation in non-prototype mode.
     * </p>
     */
    Map<String, Collection<String>> messagesWithContext() {
        return contextsByMsgId.asMap();
    }

}
