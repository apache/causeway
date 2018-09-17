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
package org.apache.isis.core.runtime.services.i18n.po;

import java.util.SortedMap;
import java.util.SortedSet;
import com.google.common.collect.Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.runtime.system.context.IsisContext;

class PoWriter extends PoAbstract {

    public static Logger LOG = LoggerFactory.getLogger(PoWriter.class);

    private static class Block {
        private final String msgId;
        private final SortedSet<String> contexts = _Sets.newTreeSet();
        private String msgIdPlural;

        private Block(final String msgId) {
            this.msgId = msgId;
        }
    }

    private final SortedMap<String, Block> blocksByMsgId = Maps.newTreeMap();

    public PoWriter(final TranslationServicePo translationServicePo) {
        super(translationServicePo, TranslationService.Mode.WRITE);
    }

    // -- shutdown

    @Override
    void shutdown() {
        if(IsisContext.getMetaModelInvalidExceptionIfAny() != null) {
            // suppress logging translations
            return;
        }
        logTranslations();
    }

    private void logTranslations() {
        final StringBuilder buf = new StringBuilder();

        buf.append("\n");
        buf.append("\n##############################################################################");
        buf.append("\n#");
        buf.append("\n# .pot file");
        buf.append("\n#");
        buf.append("\n# Translate this file to each required language and place in WEB-INF, eg:");
        buf.append("\n#");
        buf.append("\n#     /WEB-INF/translations-en_US.po");
        buf.append("\n#     /WEB-INF/translations-en.po");
        buf.append("\n#     /WEB-INF/translations-fr_FR.po");
        buf.append("\n#     /WEB-INF/translations-fr.po");
        buf.append("\n#     /WEB-INF/translations.po");
        buf.append("\n#");
        buf.append("\n# If the app uses TranslatableString (eg for internationalized validation");
        buf.append("\n# messages), or if the app calls the TranslationService directly, then ensure");
        buf.append("\n# that all text to be translated has been captured by running a full");
        buf.append("\n# integration test suite that exercises all relevant behaviour");
        buf.append("\n#");
        buf.append("\n##############################################################################");
        buf.append("\n");
        buf.append("\n");
        toPot(buf);
        buf.append("\n");
        buf.append("\n");
        buf.append("\n##############################################################################");
        buf.append("\n# end of .pot file");
        buf.append("\n##############################################################################");
        buf.append("\n");
        LOG.info(buf.toString());
    }

    /**
     * As per <a href="http://pology.nedohodnik.net/doc/user/en_US/ch-poformat.html">section 2.6</a>.
     */
    protected void header(final StringBuilder buf) {
        final String createdAt = LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss+Z");
        buf.append("#, fuzzy").append("\n");
        buf.append("msgid \"\"").append("\n");
        buf.append("msgstr \"\"").append("\n");
        buf.append("\"Project-Id-Version: \\n\"").append("\n");
        buf.append("\"POT-Creation-Date: ").append(createdAt).append("\\n\"").append("\n");
        buf.append("\"MIME-Version: 1.0\\n\"").append("\n");
        buf.append("\"Content-Type: text/plain; charset=UTF-8\\n\"").append("\n");
        buf.append("\"Content-Transfer-Encoding: 8bit\\n\"").append("\n");
        buf.append("\"Plural-Forms: nplurals=2; plural=n != 1;\\n\"").append("\n");
        buf.append("\n\n");
    }



    @Override
    public String translate(final String context, final String msgId) {

        if(msgId == null) {
            return null;
        }
        final Block block = blockFor(msgId);
        block.contexts.add(context);

        return msgId;
    }

    @Override
    String translate(final String context, final String msgId, final String msgIdPlural, final int num) {

        if(msgId == null) {
            return null;
        }
        final Block block = blockFor(msgId);
        block.contexts.add(context);
        block.msgIdPlural = msgIdPlural;

        return null;
    }

    private synchronized Block blockFor(final String msgId) {
        Block block = blocksByMsgId.get(msgId);
        if(block == null) {
            block = new Block(msgId);
            blocksByMsgId.put(msgId, block);
        }
        return block;
    }

    void toPot(final StringBuilder buf) {
        header(buf);
        for (final String msgId : blocksByMsgId.keySet()) {
            final Block block = blocksByMsgId.get(msgId);
            for (final String context : block.contexts) {
                buf.append("#: ").append(context).append("\n");
            }
            buf.append("msgid \"").append(escape(msgId)).append("\"\n");
            String msgIdPlural = block.msgIdPlural;
            if(msgIdPlural == null) {
                buf.append("msgstr \"\"\n");
            } else {
                buf.append("msgid_plural \"").append(escape(msgIdPlural)).append("\"\n");
                buf.append("msgstr[0] \"\"\n");
                buf.append("msgstr[1] \"\"\n");
            }
            buf.append("\n\n");
        }
    }

    static String escape(final String msgId) {
        return msgId.replace("\"", "\\\"");
    }

}
