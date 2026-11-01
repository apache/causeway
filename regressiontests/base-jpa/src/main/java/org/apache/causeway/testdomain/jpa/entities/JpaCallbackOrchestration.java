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
package org.apache.causeway.testdomain.jpa.entities;

import jakarta.inject.Named;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Locking;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Title;

import org.apache.causeway.persistence.jpa.applib.integration.HasVersion;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** Minimal batch callback fixture; completionEvents models an outbox publication. */
@Entity
@Named("testdomain.jpa.CallbackOrchestration")
@DomainObject(locking = Locking.PESSIMISTIC, entityChangePublishing = Publishing.DISABLED)
@NoArgsConstructor
public class JpaCallbackOrchestration implements HasVersion<Long> {
    @Id @Property @Title @Getter
    private String id;
    @Version @Property @Getter
    private Long version;
    @Property @Getter
    private int completedLines;
    @Property @Getter
    private int completionEvents;

    public JpaCallbackOrchestration(final String id) {
        this.id = id;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public void onSuccess(final int line) {
        completedLines |= 1 << line;
        if (completedLines == 3 && completionEvents == 0) {
            completionEvents++;
        }
    }
}
