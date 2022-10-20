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
package org.apache.causeway.applib.services.email;

import java.io.Serializable;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;

/**
 *
 * The `EmailService` provides the ability to send HTML emails, with
 * attachments, to one or more recipients.
 *
 * <p>
 *     The framework provides a default implementation to send emails using
 *     an external SMTP provider.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface EmailService extends Serializable {

    @PostConstruct
    public void init() ;

    /**
     * The main API to send the email (and optional attachments).
     *
     * @param to - pass either `null` or `Collections.emptyList()` if not required
     * @param cc - pass either `null` or `Collections.emptyList()` if not required
     * @param bcc - pass either `null` or `Collections.emptyList()` if not required
     * @param subject - a simple string, no formatting
     * @param body - should be HTML text
     * @param attachments - attachments that describe their mime type
     *
     * @return Will return `false` if failed to send
     */
    boolean send(List<String> to, List<String> cc, List<String> bcc, String subject, String body, DataSource... attachments);

    /**
     * Whether this service has been configured and thus available for use.
     *
     * @return if `false` then any attempt to call `send(...)` will throw an exception.
     */
    boolean isConfigured();

}
