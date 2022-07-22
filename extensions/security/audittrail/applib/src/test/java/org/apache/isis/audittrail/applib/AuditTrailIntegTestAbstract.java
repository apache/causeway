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
package org.apache.isis.audittrail.applib;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Qualifier;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.audittrail.applib.dom.AuditTrailEntry;
import org.apache.isis.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class AuditTrailIntegTestAbstract extends IsisIntegrationTestAbstract {

    @BeforeEach
    void setUp() {
    }


    @Inject @Qualifier("audittrail") EntityPropertyChangeSubscriber entityPropertyChangeSubscriber;
    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;

}
