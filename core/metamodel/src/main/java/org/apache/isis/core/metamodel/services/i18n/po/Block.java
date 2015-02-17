package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Lists;

class Block {

    private enum State {
        CONTEXT("^#: (?<value>.+)$"),
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

    State state = State.CONTEXT;

    List<String> contextList = Lists.newArrayList();
    String msgid = null;
    String msgid_plural = null;
    String msgstr = null; // either from msgstr or msgstr[0] if there is a plural
    String msgstr_plural = null; // from msgstr[1]

    Block parseLine(final String line, final Map<MsgIdAndContext, String> translationsByKey) {
        if (state == State.CONTEXT) {
            final Matcher contextMatcher = state.pattern.matcher(line);
            if (contextMatcher.matches()) {
                final String context = contextMatcher.group("value");
                contextList.add(context);
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

    void append(final Map<MsgIdAndContext, String> translationsByKey) {
        if(msgid != null && msgstr != null) {
            for (String context : contextList) {
                final MsgIdAndContext mc = new MsgIdAndContext(msgid, context);
                translationsByKey.put(mc, msgstr);
            }
        }
    }

}
