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
package org.apache.isis.runtimeservices.error;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.error.ErrorDetails;
import org.apache.isis.applib.services.error.ErrorReportingService;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

/**
 * Response from the {@link ErrorReportingService}, containing information to show to the end-user.
 *
 * <p>
 *     Implementation notes:
 *     <ul>
 *         <li>a class has been used here so that additional fields might be added in the future.</li>
 *         <li>the class is {@link Serializable}</li> so that it can be stored by the Wicket viewer as a Wicket model.
 *     </ul>
 * </p>
 */
public class EmailTicket extends SimpleTicket {

    // -- MAILTO VALUE TYPE

    public static class MailTo implements Serializable {

        private static final long serialVersionUID = -817872853109724987L;

        public static class MailToBuilder {
            private final MailTo mailTo = new MailTo();
            public MailTo build() {
                return mailTo;
            }
            public MailToBuilder linkName(String linkName) {
                mailTo.linkName = linkName;
                return this;
            }
            public MailToBuilder receiver(String receiver) {
                mailTo.receiver = receiver;
                return this;
            }
            public MailToBuilder subject(String subject) {
                mailTo.subject = subject;
                return this;
            }
            public MailToBuilder body(String body) {
                mailTo.body = body;
                return this;
            }
        }

        public static MailToBuilder builder() {
            return new MailToBuilder();
        }

        private String linkName = "Email";
        private String receiver = "no-one@nowhere";
        private String subject = "[Module-Name] Unexpected Error (#ref)";
        private String body = "empty body";

        public String toHtmlLink() {
            return String.format("<a href=\"mailto:%s?subject=%s&body=%s\">%s</a>",
                    receiver,
                    htmlEscape(subject),
                    htmlEscape(body),
                    linkName
                    );
        }

        // -- STACKTRACE FORMATTING

        public static String mailBodyOf(ErrorDetails errorDetails) {
            return "Stacktrace:%0D%0A=================%0D%0A" +
                    stream(errorDetails.getStackTraceDetailPerCause())
            .map(MailTo::causeToString)
            .collect(Collectors.joining("%0D%0A%0D%0A"))
            ;
        }

        private static String causeToString(List<String> list) {
            return "Cause%0D%0A------------%0D%0A" +
                    stream(list)
            .map(entry->String.format("# %s", entry))
            .collect(Collectors.joining("%0D%0A"))
            ;
        }


    }

    // TICKET IMPL

    private static final long serialVersionUID = -748973805361941912L;
    private MailTo mailTo;

    public EmailTicket(
            MailTo mailTo,
            String reference,
            String userMessage,
            String details,
            StackTracePolicy stackTracePolicy,
            String kittenUrl) {
        super(reference, userMessage, details, stackTracePolicy, kittenUrl);
        this.mailTo = mailTo;
    }

    @Override
    public String getMarkup() {
        return
                "<p>" +
                ifPresentMap(getDetails(), s->"<h3>" + htmlEscape(s) + "</h3>") +
                ifPresentMap(getKittenUrl(), s->"<img src=\"" + s + "\"></img>") +
                "</p>" +
                ifPresentMap(getReference(), s->
                "<p><h4>Please report this error: <span>" + mailTo.toHtmlLink() + "</span></h4></p>")
                ;
    }



}
