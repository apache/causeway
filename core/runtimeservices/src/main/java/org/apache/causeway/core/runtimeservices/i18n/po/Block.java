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
package org.apache.causeway.core.runtimeservices.i18n.po;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.causeway.commons.internal.collections._Lists;

class Block {

    private enum State {
        /**
         * XXX[CAUSEWAY-2987] in support of (not providing any context) ...
         * <pre>
         * #:
         * msgid "Anonymous"
         * msgstr "Anonim felhasználó"
         * </pre>
         */
        ROOT_CONTEXT("^#:\\s*$"),
        SPECIFIC_CONTEXT("^#: (?<value>.+)$"),
        MSGID("^msgid \"(?<value>.+)\"$"),
        MSGID_PLURAL("^msgid_plural \"(?<value>.+)\"$"),
        MSGSTR("^msgstr \"(?<value>.+)\"$"),
        MSGSTR0("^msgstr\\[0\\] \"(?<value>.+)\"$"),
        MSGSTR1("^msgstr\\[1\\] \"(?<value>.+)\"$");

        private final Pattern pattern;

        private State(final String regex) {
            pattern = Pattern.compile(regex);
        }
    }

    State state = State.SPECIFIC_CONTEXT;

    List<String> contextList = _Lists.newArrayList();
    String msgid = null;
    String msgid_plural = null;
    String msgstr = null; // either from msgstr or msgstr[0] if there is a plural
    String msgstr_plural = null; // from msgstr[1]

    Block parseLine(final String line, final Map<ContextAndMsgId, String> translationsByKey) {

        if (state == State.SPECIFIC_CONTEXT) {
            final Matcher contextMatcher = state.pattern.matcher(line);
            if (contextMatcher.matches()) {
                final String context = contextMatcher.group("value");
                contextList.add(context);
                return this;
            } else {
                state = State.ROOT_CONTEXT;
            }
        }

        if (state == State.ROOT_CONTEXT) {
            final Matcher contextMatcher = state.pattern.matcher(line);
            if (contextMatcher.matches()) {
                contextList.add("");
                return this;
            } else {
                state = State.MSGID;
                // fallthrough (there may not have been any more context)
            }
        }

        if (state == State.MSGID) {
            final Matcher msgidMatcher = state.pattern.matcher(line);
            if (msgidMatcher.matches()) {
                msgid = msgidMatcher.group("value");
                state = State.MSGID_PLURAL; // found, next time look for plurals
            } else {
                return new Block();
            }
            return this;
        }

        if (state == State.MSGID_PLURAL) {
            final Matcher msgIdPluralMatcher = state.pattern.matcher(line);
            if (msgIdPluralMatcher.matches()) {
                msgid_plural = msgIdPluralMatcher.group("value");
                state = State.MSGSTR0; // next time look for msgstr[0]
                return this;
            } else {
                state = State.MSGSTR; // fall through (there may not have been any plural form)
            }
        }

        if (state == State.MSGSTR) {
            final Matcher msgStrMatcher = state.pattern.matcher(line);
            if (msgStrMatcher.matches()) {
                msgstr = msgStrMatcher.group("value");
            }
            append(translationsByKey);
            return new Block();
        }

        if (state == State.MSGSTR0) {
            final Matcher msgStr0Matcher = state.pattern.matcher(line);
            if (msgStr0Matcher.matches()) {
                msgstr = msgStr0Matcher.group("value");
                state = State.MSGSTR1; // next time, look for plural
            } else {
                append(translationsByKey);
                return new Block();
            }
            return this;
        }

        if (state == State.MSGSTR1) {
            final Matcher msgStr1Matcher = state.pattern.matcher(line);
            if (msgStr1Matcher.matches()) {
                msgstr_plural = msgStr1Matcher.group("value");
            }
            append(translationsByKey);
            return new Block();
        }
        return this;
    }

    void append(final Map<ContextAndMsgId, String> translationsByKey) {
        for (String context : contextList) {
            if(msgid != null && msgstr != null) {
                final ContextAndMsgId mc = new ContextAndMsgId(context, msgid, ContextAndMsgId.Type.REGULAR);
                translationsByKey.put(mc, msgstr);
            }
            if(msgid_plural != null && msgstr_plural != null) {
                final ContextAndMsgId mc = new ContextAndMsgId(context, msgid_plural, ContextAndMsgId.Type.PLURAL_ONLY);
                translationsByKey.put(mc, msgstr_plural);
            }
        }
    }

}
