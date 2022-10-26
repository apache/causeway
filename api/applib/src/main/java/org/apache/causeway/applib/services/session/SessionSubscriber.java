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
package org.apache.causeway.applib.services.session;

import java.util.Date;
import java.util.UUID;

/**
 * Defines an API to track the status of the current sessions
 * (&quot;users logged on&quot;) on the system.
 *
 * <p>
 *     Multiple implementations can be registered; all will be called.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface SessionSubscriber {

    enum Type {
        LOGIN,
        LOGOUT
    }

    enum CausedBy {
        USER,
        SESSION_EXPIRATION,
        RESTART
    }

    /**
     * Callback to log the session.
     *
     * @param type
     * @param username
     * @param date
     * @param causedBy
     * @param sessionGuid - guaranteed to be unique
     * @param httpSessionId - generally expected to be unique, provided to correlate with other logs
     */
    void log(
            Type type,
            String username,
            Date date,
            CausedBy causedBy,
            UUID sessionGuid,
            String httpSessionId);


}
