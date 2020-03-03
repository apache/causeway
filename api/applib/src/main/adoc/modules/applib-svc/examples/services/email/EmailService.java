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
package org.apache.isis.applib.services.email;

import java.io.Serializable;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Provides the ability to send emails to one or more recipients.
 *
 * <p>
 * The core framework also provides a default implementation <tt>EmailServiceDefault</tt> that sends email as an
 * HTML message, using an external SMTP provider.  See the Isis website for further details.
 * </p>
 */
// tag::refguide[]
public interface EmailService extends Serializable {

    @PostConstruct
    public void init() ;

    boolean send(List<String> to, List<String> cc, List<String> bcc, String subject, String body, DataSource... attachments);

    // end::refguide[]
    /**
     * Whether this service has been configured and thus available for use.
     */
    // tag::refguide[]
    boolean isConfigured();

}
// end::refguide[]
