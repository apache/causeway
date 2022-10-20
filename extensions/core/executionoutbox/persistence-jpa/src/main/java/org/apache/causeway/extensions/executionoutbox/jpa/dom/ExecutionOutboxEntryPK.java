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
package org.apache.causeway.extensions.executionoutbox.jpa.dom;

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
public class ExecutionOutboxEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "_";

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(name = ExecutionOutboxEntry.InteractionId.NAME, nullable = ExecutionOutboxEntry.InteractionId.NULLABLE, length = ExecutionOutboxEntry.InteractionId.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID interactionId;

    @Column(name = ExecutionOutboxEntry.Sequence.NAME, nullable = ExecutionOutboxEntry.Sequence.NULLABLE)
    @Getter(AccessLevel.PACKAGE)
    public int sequence;

    public ExecutionOutboxEntryPK(final String value) {
        val token = new StringTokenizer (value, SEPARATOR);
        this.interactionId = UUID.fromString(token.nextToken());
        this.sequence = Integer.parseInt(token.nextToken());
    }

    @Override
    public String toString() {
        return interactionId + SEPARATOR + sequence;
    }

    @Component
    @Priority(PriorityPrecedence.MIDPOINT)
    public static class Semantics
    extends ValueSemanticsBasedOnIdStringifierEntityAgnostic<ExecutionOutboxEntryPK> {

        public Semantics() {
            super(ExecutionOutboxEntryPK.class);
        }

        @Override
        public String enstring(final ExecutionOutboxEntryPK value) {
            return value.toString();
        }

        @Override
        public ExecutionOutboxEntryPK destring(
                final @NonNull String stringified) {
            return new ExecutionOutboxEntryPK(stringified);
        }

        @Override
        public boolean isValid(@NonNull final ExecutionOutboxEntryPK value) {
            return value.getInteractionId()!=null;
        }

    }

}
