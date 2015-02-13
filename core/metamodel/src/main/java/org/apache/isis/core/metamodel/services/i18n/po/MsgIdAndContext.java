package org.apache.isis.core.metamodel.services.i18n.po;

/**
 * The combination of a <tt>msgId</tt> and context (optionally null) that represents a key to a translatable resource.
 *
 * <p>
 *     For example, with this <i>.pot</i> file:
 * </p>
 * <pre>
 * #: org.isisaddons.module.sessionlogger.dom.SessionLoggingServiceMenu#activeSessions()
 msgid: "Active Sessions"

 #: org.isisaddons.module.audit.dom.AuditingServiceMenu
 #: org.isisaddons.module.command.dom.CommandServiceMenu
 #: org.isisaddons.module.publishing.dom.PublishingServiceMenu
 msgid: "Activity"

 * </pre>
 *
 * <p>
 *     the combination of <code>{org.isisaddons.module.sessionlogger.dom.SessionLoggingServiceMenu#activeSessions(), "Active Sessions"}</code> represents such a key, as does <code>{org.isisaddons.module.audit.dom.AuditingServiceMenu, "Activity"}</code>
 * </p>
 */
public class MsgIdAndContext implements Comparable<MsgIdAndContext> {

    private final String context;
    private final String msgId;

    public MsgIdAndContext(final String msgId, final String context) {
        this.msgId = msgId;
        this.context = context == null? "": context;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getContext() {
        return context;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MsgIdAndContext that = (MsgIdAndContext) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (msgId != null ? !msgId.equals(that.msgId) : that.msgId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + (msgId != null ? msgId.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final MsgIdAndContext o) {
        final int i = msgId.compareTo(o.msgId);
        if(i != 0) {
            return i;
        }
        return context.compareTo(o.context);
    }
}
