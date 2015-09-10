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
package org.apache.isis.core.runtime.system.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;

public class FrameworkSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(FrameworkSynchronizer.class);

    private final PersistenceSession persistenceSession;
    private final AuthenticationSession authenticationSession;

    public FrameworkSynchronizer(
            final PersistenceSession persistenceSession,
            final AuthenticationSession authenticationSession) {
        this.persistenceSession = persistenceSession;
        this.authenticationSession = authenticationSession;

    }

    /**
     * Categorises where called from.
     * 
     * <p>
     * Just used for logging.
     */
    public enum CalledFrom {
        EVENT_LOAD,
        EVENT_PRESTORE,
        EVENT_POSTSTORE,
        EVENT_PREDIRTY,
        EVENT_POSTDIRTY,
        OS_QUERY,
        OS_RESOLVE,
        OS_LAZILYLOADED,
        EVENT_PREDELETE,
        EVENT_POSTDELETE
    }




}
