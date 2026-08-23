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
package demoapp.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.audittrail.jpa.CausewayModuleExtAuditTrailPersistenceJpa;
import org.apache.causeway.extensions.commandlog.jpa.CausewayModuleExtCommandLogPersistenceJpa;
import org.apache.causeway.extensions.executionlog.jpa.CausewayModuleExtExecutionLogPersistenceJpa;
import org.apache.causeway.extensions.executionoutbox.jpa.CausewayModuleExtExecutionOutboxPersistenceJpa;
import org.apache.causeway.extensions.secman.jpa.CausewayModuleExtSecmanPersistenceJpa;
import org.apache.causeway.extensions.sessionlog.jpa.CausewayModuleExtSessionLogPersistenceJpa;

import demoapp.dom.ReferenceModuleJpa;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@Import({
    ReferenceModuleJpa.class,
    ReferenceAppManifestCommon.class,

    // Security Manager Extension (secman)
    CausewayModuleExtSecmanPersistenceJpa.class,
    CausewayModuleExtSessionLogPersistenceJpa.class,
    CausewayModuleExtCommandLogPersistenceJpa.class,
    CausewayModuleExtExecutionLogPersistenceJpa.class,
    CausewayModuleExtExecutionOutboxPersistenceJpa.class,
    CausewayModuleExtAuditTrailPersistenceJpa.class,
})
public class ReferenceAppManifestJpa {

}
