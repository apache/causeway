/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.sessionlog.jpa.dom;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Priority;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.ValueSemanticsBasedOnIdStringifierEntityAgnostic;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode
public class SessionLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(
            name = SessionLogEntry.SessionGuid.NAME,
            nullable = SessionLogEntry.SessionGuid.NULLABLE,
            length = SessionLogEntry.SessionGuid.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID sessionGuid;

    @Override
    public String toString() {
        return sessionGuid != null
                ? sessionGuid.toString()
                : null;
    }

    @Component
    @Priority(PriorityPrecedence.MIDPOINT)
    public static class Semantics
    extends ValueSemanticsBasedOnIdStringifierEntityAgnostic<SessionLogEntryPK> {

        public Semantics() {
            super(SessionLogEntryPK.class);
        }

        @Override
        public String enstring(final SessionLogEntryPK value) {
            // fails if no guid
            return value.getSessionGuid().toString();
        }

        @Override
        public SessionLogEntryPK destring(final @NonNull String stringifiedUuid) {
            return new SessionLogEntryPK(UUID.fromString(stringifiedUuid));
        }

        @Override
        public boolean isValid(@NonNull final SessionLogEntryPK value) {
            return value.getSessionGuid()!=null;
        }
    }
}
