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
package demoapp.dom.services.core.errorreportingservice;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.error.ErrorDetails;
import org.apache.causeway.applib.services.error.ErrorReportingService;
import org.apache.causeway.applib.services.error.SimpleTicket;
import org.apache.causeway.commons.internal.base._Strings;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.Builder;

/**
 * Response from the {@link ErrorReportingService}, containing information to show to the end-user.
 *
 * <p>
 *     Implementation notes:
 *     <ul>
 *         <li>The mailTo link has an arbitrary 1000 character limit for the mailTo body.
 *         For production, one would implement a more sophisticated error reporting feature,
 *         that sends e-mails directly from the server.</li>
 *         <li>A class has been used here so that additional fields might be added in the future.</li>
 *         <li>The class is {@link Serializable}</li> so that it can be stored by the Wicket viewer as a Wicket model.
 *     </ul>
 * </p>
 */
//tag::class[]
public class EmailTicket extends SimpleTicket {
    // ...
//end::class[]

//tag::mailTo[]
    @Builder
    public static class MailTo implements Serializable {
    // ...
//end::mailTo[]

        private static final long serialVersionUID = -817872853109724987L;

        @Builder.Default private String linkName = "Email";
        @Builder.Default private String receiver = "no-one@nowhere";
        @Builder.Default private String subject = "[Module-Name] Unexpected Error (#ref)";
        @Builder.Default private String body = "empty body";

//tag::mailTo[]
        public String toHtmlLink() {
            var messageProperties = Map.<String, Object>of(
                "receiver", receiver,
                "subject",  htmlEscape(subject),
                "body",     htmlEscape(
                            _Strings.ellipsifyAtEnd(body, 1000, "... truncated")),  // <.>
                "linkName", linkName);

            return _Strings.format(
                    "<a href=\"mailto:${receiver}?subject=${subject}&body=${body}\">${linkName}</a>",
                    messageProperties);
        }
//end::mailTo[]

        static String mailBodyOf(final ErrorDetails errorDetails) {
            return "Stacktrace:%0D%0A=================%0D%0A" +
                    stream(errorDetails.stackTraceDetailPerCause())
            .map(MailTo::causeToString)
            .collect(Collectors.joining("%0D%0A%0D%0A"))
            ;
        }

        private static String causeToString(final List<String> list) {
            return "Cause%0D%0A------------%0D%0A" +
                    stream(list)
            .map(entry->String.format("# %s", entry))
            .collect(Collectors.joining("%0D%0A"))
            ;
        }
//tag::mailTo[]
    }
//end::mailTo[]

    private static final long serialVersionUID = -748973805361941912L;
    private MailTo mailTo;

//tag::class[]
    public EmailTicket(
            final MailTo mailTo,
            final String reference,
            final String userMessage,
            final String details,
            final StackTracePolicy stackTracePolicy,
            final String kittenUrl) {                   // <.>
        super(reference, userMessage, details, stackTracePolicy, kittenUrl);
        this.mailTo = mailTo;
    }
//end::class[]

//tag::markup[]
    @Override
    public String getMarkup() {
        var messageProperties = Map.<String, Object>of(
            "title", ifPresentMap(getDetails(), details-> String.format("<h3>%s</h3>", htmlEscape(details))),
            "img",   ifPresentMap(getKittenUrl(), kittenUrl-> String.format("<img src=\"%s\"></img>", kittenUrl)),
            "para",  String.format("<p><h4>Please report this error: <span>%s</span></h4></p>", mailTo.toHtmlLink()));
        return _Strings.format(
                "<p>${title}${img}</p>${para}",
                messageProperties);
    }
//end::markup[]
//tag::class[]
}
//end::class[]
