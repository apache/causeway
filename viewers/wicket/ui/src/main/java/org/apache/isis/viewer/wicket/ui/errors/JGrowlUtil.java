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
package org.apache.isis.viewer.wicket.ui.errors;

import org.apache.wicket.util.string.Strings;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.interaction.session.MessageBroker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class JGrowlUtil {

    private JGrowlUtil(){}

    @RequiredArgsConstructor @Getter
    static enum MessageSeverity {
        INFO(3500) {
            @Override long delay(IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getInfoDelay().toMillis();
            }
        },
        WARNING(0) {
            @Override long delay(IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getWarningDelay().toMillis();
            }
        }, // sticky
        DANGER(0){
            @Override long delay(IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getErrorDelay().toMillis();
            }
        } // sticky
        ;

        private final int delayMillis;

        public String cssClassSuffix() {
            return name().toLowerCase();
        }

        abstract long delay(IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups);
    }

    public static String asJGrowlCalls(final MessageBroker messageBroker, IsisConfiguration configuration) {
        val buf = new StringBuilder();

        val messagePopups = configuration.getViewer().getWicket().getMessagePopups();
        for (String info : messageBroker.drainMessages()) {
            addJGrowlCall(info, JGrowlUtil.MessageSeverity.INFO, messagePopups, buf);
        }

        for (String warning : messageBroker.drainWarnings()) {
            addJGrowlCall(warning, JGrowlUtil.MessageSeverity.WARNING, messagePopups, buf);
        }

        messageBroker.drainApplicationError()
        .ifPresent(error->
            addJGrowlCall(error, MessageSeverity.DANGER, messagePopups, buf));

        return buf.toString();
    }

    private static void addJGrowlCall(
            final String origMsg,
            final MessageSeverity severity,
            final IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups,
            final StringBuilder buf) {

        final CharSequence escapedMsg = escape(origMsg);
        buf.append("$.growl(\"")
        .append(escapedMsg)
        .append("&#160;&#160;&#160;") // add some space so that the dismiss icon (x) doesn't overlap with the text
        .append('"');
        buf.append(", {");
        buf.append("type: \"").append(severity.cssClassSuffix()).append('"');
        buf.append(String.format(", delay: %d", severity.delay(messagePopups)));
        buf.append(String.format(", placement: { from: '%s', align: '%s' }", messagePopups.getPlacement().getVertical().name().toLowerCase(), messagePopups.getPlacement().getHorizontal().name().toLowerCase()));
        buf.append(String.format(", offset: %d", messagePopups.getOffset()));
        buf.append('}');
        buf.append(");\n");
    }

    static String escape(String origMsg) {
        final String escaped = Strings.escapeMarkup(origMsg).toString();

        // convert (what would originally have been either) ' or " to '
        return escaped
                .replace("&quot;", "'")
                .replace("&#039;", "'");
    }
}
