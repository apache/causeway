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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.extensions.audittrail.jpa.dom.AuditTrailEntry;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.extensions.executionlog.jpa.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.secman.jpa.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.jpa.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.jpa.user.dom.ApplicationUser;
import org.apache.causeway.extensions.sessionlog.jpa.dom.SessionLogEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
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
    void audittrail_applib() {
    	verifier.verify(org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry.class);
    }

    @Test
    void commandlog() {
    	verifier.verify(CommandLogEntry.class);
    }
    @Test
    void commandlog_applib() {
    	verifier.verify(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry.class);
    }

    @Test
    void executionlog() {
    	verifier.verify(ExecutionLogEntry.class);
    }
    @Test
    void executionlog_applib() {
    	verifier.verify(org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry.class);
    }

//no JPA variant of ExcelDemoToDoItem in v2 yet (however, could be backported from main)    
//    @Test
//    void exceldemo() {
//        assertTrue(classCache.isByteCodeEnhanced(ExcelDemoToDoItem.class));
//    }

    @Test
    void executionoutbox() {
    	verifier.verify(ExecutionOutboxEntry.class);
    }
    @Test
    void executionoutbox_applib() {
    	verifier.verify(org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry.class);
    }

    @Test void secman_role() {
    	verifier.verify(ApplicationRole.class);
    }
    @Test void secman_permission() {
    	verifier.verify(ApplicationPermission.class);
    }
    @Test void secman_tenancy() {
    	verifier.verify(ApplicationTenancy.class);
    }
    @Test void secman_user() {
    	verifier.verify(ApplicationUser.class);
    }
    @Test void secman_role_applib() {
    	verifier.verify(org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole.class);
    }
    @Test void secman_permission_applib() {
    	verifier.verify(org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission.class);
    }
    @Test void secman_tenancy_applib() {
    	verifier.verify(org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy.class);
    }
    @Test void secman_user_applib() {
    	verifier.verify(org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser.class);
    }

    @Test
    void sessionlog() {
    	verifier.verify(SessionLogEntry.class);
    }
    @Test
    void sessionlog_applib() {
    	verifier.verify(org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry.class);
    }
    
    // -- HELPER

    @RequiredArgsConstructor
    final static class Verifier {

    	final Version expectedVersion; 
    	final _ClassCache classCache;
    	
    	@RequiredArgsConstructor
    	final static class Version {
    		final int major; final int minor;
    		@Override
    		public boolean equals(Object obj) {
    			return obj instanceof Version
    					? ((Version)obj).major == major
    						&& ((Version)obj).minor == minor
    					: false;
    		}
    		@Override
    		public int hashCode() {
    			return super.hashCode();
    		}
    	}

        Verifier(final Version expectedVersion) {
            this(expectedVersion, _ClassCache.getInstance());
        }

        void verify(final Class<?> cls) {
            assertEquals(expectedVersion, getVersion(cls), ()->"java byte-code version mismatch");
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
