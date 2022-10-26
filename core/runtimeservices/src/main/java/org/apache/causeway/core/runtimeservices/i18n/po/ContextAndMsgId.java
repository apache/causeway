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

/**
 * The combination of a <tt>msgId</tt> and context (optionally null) that represents a key to a translatable resource.
 *
 * <p>
 *     For example, with this <i>.pot</i> file:
 * </p>
 * <pre>
 * #: org.causewayaddons.module.sessionlogger.dom.SessionLoggingServiceMenu#activeSessions()
 msgid: "Active Sessions"

 #: org.causewayaddons.module.audit.dom.AuditingServiceMenu
 #: org.causewayaddons.module.command.dom.CommandServiceMenu
 #: org.causewayaddons.module.publishing.dom.PublishingServiceMenu
 msgid: "Activity"

 * </pre>
 *
 * <p>
 *     the combination of <code>{org.causewayaddons.module.sessionlogger.dom.SessionLoggingServiceMenu#activeSessions(), "Active Sessions"}</code> represents such a key, as does <code>{org.causewayaddons.module.audit.dom.AuditingServiceMenu, "Activity"}</code>
 * </p>
 */
public class ContextAndMsgId implements Comparable<ContextAndMsgId> {

    public enum Type {
        /**
         * The text to use when there is no plural form, or the text to use for singular pattern when there is also a plural form.
         */
        REGULAR,
        /**
         * The text to use for plural form.
         */
        PLURAL_ONLY
    }

    private final String context;
    private final String msgId;
    private final Type type;

    public ContextAndMsgId(final String context, final String msgId, final Type type) {
        this.context = context == null? "": context;
        this.msgId = msgId;
        this.type = type;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getContext() {
        return context;
    }

    /**
     * Not part of equals/hashCode impl.
     */
    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ContextAndMsgId that = (ContextAndMsgId) o;

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
    public int compareTo(final ContextAndMsgId o) {
        final int i = msgId.compareTo(o.msgId);
        if(i != 0) {
            return i;
        }
        return context.compareTo(o.context);
    }

    @Override
    public String toString() {
        return "ContextAndMsgId{" +
                "context='" + context + '\'' +
                ", msgId='" + msgId + '\'' +
                '}';
    }
}
