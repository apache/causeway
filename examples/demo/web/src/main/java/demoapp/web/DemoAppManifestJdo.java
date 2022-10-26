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

import org.apache.causeway.extensions.audittrail.jdo.CausewayModuleExtAuditTrailPersistenceJdo;
import org.apache.causeway.extensions.commandlog.jdo.CausewayModuleExtCommandLogPersistenceJdo;
import org.apache.causeway.extensions.executionlog.jdo.CausewayModuleExtExecutionLogPersistenceJdo;
import org.apache.causeway.extensions.executionoutbox.jdo.CausewayModuleExtExecutionOutboxPersistenceJdo;
import org.apache.causeway.extensions.secman.jdo.CausewayModuleExtSecmanPersistenceJdo;
import org.apache.causeway.extensions.sessionlog.jdo.CausewayModuleExtSessionLogPersistenceJdo;

import demoapp.dom.DemoModuleJdo;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@Import({
    DemoModuleJdo.class,
    DemoAppManifestCommon.class,

    // Security Manager Extension (secman)
    CausewayModuleExtSecmanPersistenceJdo.class,
    CausewayModuleExtSessionLogPersistenceJdo.class,
    CausewayModuleExtCommandLogPersistenceJdo.class,
    CausewayModuleExtExecutionLogPersistenceJdo.class,
    CausewayModuleExtExecutionOutboxPersistenceJdo.class,
    CausewayModuleExtAuditTrailPersistenceJdo.class,


})
public class DemoAppManifestJdo {

}
