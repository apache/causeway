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
package org.apache.isis.applib.services.audit;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;

/**
 * Will be called whenever an auditable entity has changed its state.
 *
 * <p>
 * Typically entities are marked as auditable using the {@link org.apache.isis.applib.annotation.Audited}
 * annotation.
 *
 * <p>
 * There are currently two implementations, <tt>AuditingServiceJdo</tt> (part of the
 * <tt>o.a.i.module:isis-module-audit-jdo</tt>) and the demo
 * {@link org.apache.isis.applib.services.audit.AuditingService3.Stderr}.
 *
 * <p>
 * To use either service, must include on the classpath and also register the service (eg in <tt>isis.properties</tt>).
 */
public interface AuditingService3 {
    
    @Programmatic
    public void audit(
            final UUID transactionId, String targetClassName, final Bookmark target, 
            String memberIdentifier, final String propertyName, 
            final String preValue, final String postValue, 
            final String user, final java.sql.Timestamp timestamp);
    
    
    public static class Stderr implements AuditingService3 {

        @Programmatic
        @Override
        public void audit(
                final UUID transactionId, final String targetClassName, final Bookmark target, 
                final String memberId, final String propertyName, 
                final String preValue, final String postValue, 
                final String user, final Timestamp timestamp) {
            String auditMessage = target.toString() + " by " + user + ", " + propertyName +": " + preValue + " -> " + postValue;
            System.err.println(auditMessage);
        }
    }

}
