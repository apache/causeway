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
package org.apache.isis.applib.services.error;

import java.io.Serializable;

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
// tag::refguide[]
public interface Ticket extends Serializable {

    // end::refguide[]
    // tag::refguide-2[]
    enum StackTracePolicy {
        SHOW,
        HIDE
    }
    // end::refguide-2[]

    /**
     * A unique identifier that the end-user can use to track any follow-up from this error.
     *
     * <p>
     *     For example, an implementation might automatically log an issue in a bug tracking system such as JIRA, in
     *     which case the {@link #getReference() reference} would be the JIRA issue number <tt>XXX-1234</tt>.
     * </p>
     */
    // tag::refguide[]
    String getReference();

    // end::refguide[]
    /**
     * Message to display to the user.
     *
     * <p>
     *     Typically this message should be short, one line long.
     * </p>
     */
    // tag::refguide[]
    String getUserMessage();

    // end::refguide[]
    /**
     * Optional additional details to show to the end-user.
     *
     * <p>
     *     For example, these might include text on how to recover from the error, or workarounds, or just further
     *     details on contacting the help desk if the issue is severe and requires immediate attention.
     * </p>
     */
    // tag::refguide[]
    String getDetails();

    // end::refguide[]
    /**
     * Whether the stack trace for the exception should be displayed or be hidden.
     *
     * <p>
     *     The default is to hide it on the basis that the reporting service will have reported the stack trace to
     *     the support team, meaning there's no need to expose this info to the end-user.
     * </p>
     */
    // tag::refguide[]
    StackTracePolicy getStackTracePolicy();

    // end::refguide[]
    /**
     * Returns a Html representation of this ticket.
     */
    // tag::refguide[]
    String getMarkup();

}
// end::refguide[]
