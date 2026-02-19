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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.util.StringUtils;

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

import lombok.SneakyThrows;

@EnabledIf(value = "isWeavingEnabled")
class VerifyExtensionEntitiesAreEnhancedTest {

    static boolean isWeavingEnabled() {
        return StringUtils.hasLength(System.getProperty("enhanceEclipselink"))
                || StringUtils.hasLength(System.getenv("enhanceEclipselink"));
    }

    private Verifier verifier = new Verifier(Verifier.getVersion(VerifyExtensionEntitiesAreEnhancedTest.class));

    @Test
    void audittrail() {
        verifier.verify(AuditTrailEntry.class);
    }

    @Test
    void commandlog() {
        verifier.verify(CommandLogEntry.class);
    }

    @Test
    void executionlog() {
        verifier.verify(ExecutionLogEntry.class);
    }

    @Test
    void exceldemo() {
        verifier.verify(ExcelDemoToDoItem.class);
    }

    @Test
    void executionoutbox() {
        verifier.verify(ExecutionOutboxEntry.class);
    }

    @Test
    void secman() {
        verifier.verify(ApplicationRole.class);
        verifier.verify(ApplicationPermission.class);
        verifier.verify(ApplicationTenancy.class);
        verifier.verify(ApplicationUser.class);
    }

    @Test
    void sessionlog() {
        verifier.verify(SessionLogEntry.class);
    }

    // -- HELPER

    record Verifier(Version expectedVersion, _ClassCache classCache) {

        public static record Version(int major, int minor) {}

        Verifier(final Version expectedVersion) {
            this(expectedVersion, _ClassCache.getInstance());
        }

        void verify(final Class<?> cls) {
            assertEquals(expectedVersion(), getVersion(cls), ()->"java byte-code version mismatch");
            assertTrue(classCache.isByteCodeEnhanced(cls), ()->"not enhanced");
        }

        //EXPERIMENTAL code suggested by Copilot
        @SneakyThrows
        static Version getVersion(final Class<?> clazz) {
            // Build the resource path (handles inner classes too)
            String resource = clazz.getName().replace('.', '/') + ".class";

            try (InputStream in = clazz.getClassLoader() != null
                    ? clazz.getClassLoader().getResourceAsStream(resource)
                    : ClassLoader.getSystemResourceAsStream(resource)) {

                if (in == null) {
                    // Fallback: try via the class itself (works with bootstrap/jrt classes)
                    try (InputStream in2 = clazz.getResourceAsStream("/" + resource)) {
                        if (in2 == null)
                            throw new IOException("Unable to locate class resource: " + resource);
                        return readHeader(in2);
                    }
                }
                return readHeader(in);
            }
        }

        private static Version readHeader(final InputStream in) throws IOException {
            try (DataInputStream dis = new DataInputStream(in)) {
                int magic = dis.readInt();
                if (magic != 0xCAFEBABE)
                    throw new IOException("Not a valid class file (bad magic): 0x" + Integer.toHexString(magic));
                int minor = dis.readUnsignedShort();
                int major = dis.readUnsignedShort();
                return new Version(major, minor);
            }
        }

    }

}
