/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.audit;

import java.util.List;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.services.HasTransactionId;

/**
 * This service contributes an <tt>auditEntries</tt> collection to any implementation of
 * {@link org.apache.isis.applib.services.HasTransactionId}, in other words commands, audit entries and published
 * events.  This allows the user to navigate to other audited effects of the given command.
 *
 * <p>
 * Because this service influences the UI, it must be explicitly registered as a service
 * (eg using <tt>isis.properties</tt>).
 */
public class AuditingServiceJdoContributions extends AbstractService {

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    @NotContributed(As.ACTION) // ie contribute as collection
    @Render(Type.EAGERLY)
    public List<AuditEntryJdo> auditEntries(final HasTransactionId hasTransactionId) {
        return auditEntryRepository.findByTransactionId(hasTransactionId.getTransactionId());
    }
    
    // //////////////////////////////////////

    @javax.inject.Inject
    private AuditingServiceJdoRepository auditEntryRepository;

}
