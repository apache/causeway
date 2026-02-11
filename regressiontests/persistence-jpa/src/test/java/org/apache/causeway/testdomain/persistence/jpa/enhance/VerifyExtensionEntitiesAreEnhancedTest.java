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

import org.eclipse.persistence.logging.SessionLogEntry;
import org.junit.jupiter.api.Disabled;
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

class VerifyExtensionEntitiesAreEnhancedTest {

    private _ClassCache classCache = _ClassCache.getInstance();

    @Test @Disabled("weaving fails, if forced to run")
    void audittrail() {
//Failed to execute goal [32mcom.ethlo.persistence.tools:eclipselink-maven-plugin:3.0.2:weave[0m [1m(default)[0m on project [36mcauseway-extensions-audittrail-persistence-jpa[0m: [31;1mException [EclipseLink-28018] (Eclipse Persistence Services - 4.0.2.v202306161219): org.eclipse.persistence.exceptions.EntityManagerSetupException[m
//Exception Description: Predeployment of PersistenceUnit [causeway-extensions-audittrail-persistence-jpa] failed.[m
//Internal Exception: java.lang.NullPointerException: Cannot invoke "org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataClass.getName()" because the return value of "org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataClass.getSuperclass()" is null[0m[m
        assertTrue(classCache.isByteCodeEnhanced(AuditTrailEntry.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void commandlog() {
        assertTrue(classCache.isByteCodeEnhanced(CommandLogEntry.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void executionlog() {
        assertTrue(classCache.isByteCodeEnhanced(ExecutionLogEntry.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void exceldemo() {
        assertTrue(classCache.isByteCodeEnhanced(ExcelDemoToDoItem.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void executionoutbox() {
        assertTrue(classCache.isByteCodeEnhanced(ExecutionOutboxEntry.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void secman() {
        assertTrue(classCache.isByteCodeEnhanced(ApplicationRole.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationPermission.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationTenancy.class));
        assertTrue(classCache.isByteCodeEnhanced(ApplicationUser.class));
    }

    @Test @Disabled("weaving for some reason is not picked up to run")
    void sessionlog() {
        assertTrue(classCache.isByteCodeEnhanced(SessionLogEntry.class));
    }

}
