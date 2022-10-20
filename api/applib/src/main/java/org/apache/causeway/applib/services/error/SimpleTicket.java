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
package org.apache.causeway.applib.services.error;

import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.apache.causeway.commons.internal.base._Strings;

import static org.apache.causeway.commons.internal.base._NullSafe.isEmpty;

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
 *
 * @since 2.0 {@index}
 */
public class SimpleTicket implements Ticket {

    private static final long serialVersionUID = 4900947111894407314L;

    private final String reference;
    private final String userMessage;
    private final String details;
    private final StackTracePolicy stackTracePolicy;
    private final String kittenUrl;

    public SimpleTicket(final String reference, final String userMessage, final String details) {
        this(reference, userMessage, details, StackTracePolicy.HIDE);
    }

    public SimpleTicket(
            final String reference,
            final String userMessage,
            final String details,
            final StackTracePolicy stackTracePolicy) {
        this(reference, userMessage, details, stackTracePolicy, null);
    }

    public SimpleTicket(
            final String reference,
            final String userMessage,
            final String details,
            final String kittenUrl) {
        this(reference, userMessage, details, StackTracePolicy.HIDE, kittenUrl);
    }

    public SimpleTicket(
            final String reference,
            final String userMessage,
            final String details,
            final StackTracePolicy stackTracePolicy,
            final String kittenUrl) {
        this.reference = reference;
        this.userMessage = userMessage;
        this.details = details;
        this.stackTracePolicy = stackTracePolicy;
        this.kittenUrl = kittenUrl;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getUserMessage() {
        return userMessage;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public StackTracePolicy getStackTracePolicy() {
        return stackTracePolicy;
    }

    /**
     * If specified, is the external URL of an image to display to the end user.
     *
     * <p>
     *     Not necessarily of a kitten, but something by way of an apology.
     * </p>
     */
    public String getKittenUrl() {
        return kittenUrl;
    }

    @Override
    public String getMarkup() {
        return
                "<p>" +
                ifPresentMap(getDetails(), s->"<h3>" + htmlEscape(s) + "</h3>") +
                ifPresentMap(getKittenUrl(), s->"<img src=\"" + s + "\"></img>") +
                "</p>" +
                ifPresentMap(getReference(), s->
                "<p><h4>Please quote reference: <span>" + htmlEscape(s) + "</span></h4></p>")
                ;
    }

    protected static String ifPresentMap(final String x, final UnaryOperator<String> operator) {
        return isEmpty(x) ? "" : operator.apply(x);
    }

    protected static String htmlEscape(final String source) {
        return _Strings.htmlEscape(source);
    }

}
