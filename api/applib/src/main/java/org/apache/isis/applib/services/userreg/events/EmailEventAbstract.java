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
package org.apache.isis.applib.services.userreg.events;

/**
 * An event sent to all services interested in user registration
 *
 * @since 1.x {@index}
 */
public abstract class EmailEventAbstract {

    private final String email;
    private final String confirmationUrl;
    private final String applicationName;

    public EmailEventAbstract(
            final String email,
            final String confirmationUrl,
            final String applicationName) {
        this.email = email;
        this.confirmationUrl = confirmationUrl;
        this.applicationName = applicationName;
    }

    public String getEmail() {
        return email;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
