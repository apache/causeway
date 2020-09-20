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

import org.apache.isis.core.security.authentication.MessageBroker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class JGrowlUtil {

    private JGrowlUtil(){}
    
    @RequiredArgsConstructor @Getter
    static enum MessageSeverity {
        INFO(3500),
        WARNING(0), // sticky
        DANGER(0) // sticky
        ;
        
        private final int delayMillis; 
        
        public String cssClassSuffix() {
            return name().toLowerCase();
        }
    }

    public static String asJGrowlCalls(final MessageBroker messageBroker) {
        final StringBuilder buf = new StringBuilder();

        for (String info : messageBroker.getMessages()) {
            addJGrowlCall(info, JGrowlUtil.MessageSeverity.INFO, buf);
        }
        for (String warning : messageBroker.getWarnings()) {
            addJGrowlCall(warning, JGrowlUtil.MessageSeverity.WARNING, buf);
        }

        final String error =  messageBroker.getApplicationError();
        if(error!=null) {
            addJGrowlCall(error, MessageSeverity.DANGER, buf);
        }
        return buf.toString();
    }

    public static void addJGrowlCall(
            final String origMsg, 
            final MessageSeverity severity, 
            final StringBuilder buf) {
        
        final CharSequence escapedMsg = escape(origMsg);
        buf.append("$.growl(\"")
        .append(escapedMsg)
        .append("&#160;&#160;&#160;") // add some space so that the dismiss icon (x) doesn't overlap with the text
        .append('"');
        buf.append(", {");
        buf.append("type: \"").append(severity.cssClassSuffix()).append('"');
        buf.append(", delay: " + severity.delayMillis);
        buf.append(", placement: { from: 'top', align: 'right' }");
        buf.append(", offset: 50");
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
