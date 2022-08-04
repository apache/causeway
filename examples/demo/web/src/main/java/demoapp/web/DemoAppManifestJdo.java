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


import org.apache.isis.extensions.audittrail.jdo.IsisModuleExtAuditTrailPersistenceJdo;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogPersistenceJdo;
import org.apache.isis.extensions.executionlog.jdo.IsisModuleExtExecutionLogPersistenceJdo;
import org.apache.isis.extensions.executionoutbox.jdo.IsisModuleExtExecutionOutboxPersistenceJdo;
import org.apache.isis.extensions.secman.jdo.IsisModuleExtSecmanPersistenceJdo;
import org.apache.isis.extensions.sessionlog.jdo.IsisModuleExtSessionLogPersistenceJdo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import demoapp.dom.DemoModuleJdo;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@Import({
    DemoModuleJdo.class,
    DemoAppManifestCommon.class,

    // Security Manager Extension (secman)
    IsisModuleExtSecmanPersistenceJdo.class,
    IsisModuleExtSessionLogPersistenceJdo.class,
    IsisModuleExtCommandLogPersistenceJdo.class,
    IsisModuleExtExecutionLogPersistenceJdo.class,
    IsisModuleExtExecutionOutboxPersistenceJdo.class,
    IsisModuleExtAuditTrailPersistenceJdo.class,


})
public class DemoAppManifestJdo {

}
