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
package org.apache.causeway.extensions.executionlog.jpa.dom;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.annotation.Priority;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.ValueSemanticsBasedOnIdStringifierEntityAgnostic;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

@EqualsAndHashCode(of = {"interactionId", "sequence"})
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ExecutionLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "_";

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(name = ExecutionLogEntry.InteractionId.NAME, nullable = ExecutionLogEntry.InteractionId.NULLABLE, length = ExecutionLogEntry.InteractionId.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID interactionId;

    @Column(name = ExecutionLogEntry.Sequence.NAME, nullable = ExecutionLogEntry.Sequence.NULLABLE)
    @Getter(AccessLevel.PACKAGE)
    public int sequence;

    public ExecutionLogEntryPK(final String value) {
        val token = new StringTokenizer (value, SEPARATOR);
        this.interactionId = UUID.fromString(token.nextToken());
        this.sequence = Integer.parseInt(token.nextToken());
        _Assert.assertNotNull(interactionId, ()->"PK required an interactionId");
    }

    @Override
    public String toString() {
        return interactionId + SEPARATOR + sequence;
    }

    @Component
    @Priority(PriorityPrecedence.MIDPOINT)
    public static class Semantics
    extends ValueSemanticsBasedOnIdStringifierEntityAgnostic<ExecutionLogEntryPK> {

        public Semantics() {
            super(ExecutionLogEntryPK.class);
        }

        @Override
        public ExecutionLogEntryPK destring(
                final @NonNull String stringified) {
            return new ExecutionLogEntryPK(stringified);
        }

        @Override
        public boolean isValid(@NonNull final ExecutionLogEntryPK value) {
            return value.getInteractionId()!=null;
        }
    }

}
