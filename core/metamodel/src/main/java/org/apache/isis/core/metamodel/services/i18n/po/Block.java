package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Lists;

class Block {

    private enum State {
        CONTEXT,
        MSGID,
        MSGSTR
    }

    private static final Pattern contextPattern = Pattern.compile("^#: (?<context>.+)$");
    private static final Pattern msgidPattern = Pattern.compile("^msgid \"(?<msgid>.+)\"$");
    private static final Pattern msgstrPattern = Pattern.compile("^msgstr \"(?<msgstr>.+)\"$");

    State state = State.CONTEXT;

    List<String> contextList = Lists.newArrayList();
    String msgid = null;
    String msgstr = null;

    Block parseLine(final String line, final Map<MsgIdAndContext, String> translationsByKey) {
        if (state == State.CONTEXT) {
            final Matcher matcher = contextPattern.matcher(line);
            if (matcher.matches()) {
                final String context = matcher.group("context");
                contextList.add(context);
            } else {
                state = State.MSGID;
            }
        }
        if (state == State.MSGID) {
            final Matcher matcher = msgidPattern.matcher(line);
            if (matcher.matches()) {
                msgid = matcher.group("msgid");
            } else {
                state = State.MSGSTR;
            }
        }
        if (state == State.MSGSTR) {
            final Matcher matcher = msgstrPattern.matcher(line);
            if (matcher.matches()) {
                msgstr = matcher.group("msgstr");
            }
            append(translationsByKey);
            return new Block();
        }
        return this;
    }

    private void append(final Map<MsgIdAndContext, String> translationsByKey) {
        if(msgid != null && msgstr != null) {
            for (String context : contextList) {
                final MsgIdAndContext mc = new MsgIdAndContext(msgid, context);
                translationsByKey.put(mc, msgstr);
            }
        }
    }

}
