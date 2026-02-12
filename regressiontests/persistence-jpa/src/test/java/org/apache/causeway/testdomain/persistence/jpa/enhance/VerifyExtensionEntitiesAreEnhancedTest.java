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
package org.apache.causeway.testdomain.persistence.jpa.enhance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.extensions.audittrail.jpa.dom.AuditTrailEntry;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.causeway.extensions.executionlog.jpa.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.secman.jpa.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.jpa.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser;
import org.apache.causeway.extensions.sessionlog.jpa.dom.SessionLogEntry;

class VerifyExtensionEntitiesAreEnhancedTest {

    private _ClassCache classCache = _ClassCache.getInstance();

    @Test
    void audittrail() {
        assertTrue(classCache.isByteCodeEnhanced(AuditTrailEntry.class));
    }

    @Test
    void commandlog() {
        assertTrue(classCache.isByteCodeEnhanced(CommandLogEntry.class));
    }

    @Test
    void executionlog() {
        assertTrue(classCache.isByteCodeEnhanced(ExecutionLogEntry.class));
    }

    @Test
    void exceldemo() {
        assertTrue(classCache.isByteCodeEnhanced(ExcelDemoToDoItem.class));
    }

    @Test
    void executionoutbox() {
        assertTrue(classCache.isByteCodeEnhanced(ExecutionOutboxEntry.class));
    }

    @Test
    void secman() {
        assertTrue(classCache.isByteCodeEnhanced(ApplicationRole.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationPermission.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationTenancy.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationUser.class));
    }

    @Test
    void sessionlog() {
        assertTrue(classCache.isByteCodeEnhanced(SessionLogEntry.class));
    }

}
