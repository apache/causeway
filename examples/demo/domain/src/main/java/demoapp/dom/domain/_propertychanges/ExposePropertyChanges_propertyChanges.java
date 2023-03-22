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
package demoapp.dom.domain._propertychanges;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection
@RequiredArgsConstructor
public class ExposePropertyChanges_propertyChanges {
    // ...
//end::class[]

    @SuppressWarnings("unused")
    private final ExposePropertyChanges exposeexposePropertyChanges;

    @MemberSupport
//tag::class[]
    public List<? extends AuditTrailEntry> coll() {
        return auditTrailEntryRepository.findAll();
    }

    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;
}
//end::class[]
