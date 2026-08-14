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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = HasLimit_changeLimit.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(associateWith = "limit", sequence = "2", promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
@RequiredArgsConstructor
public class HasLimit_changeLimit {
    public static class DomainEvent extends HasLimit.ActionDomainEvent<HasLimit_changeLimit> { }

    private final HasLimit hasLimit;

    @MemberSupport public HasLimit act(final int newLimit) {
        return hasLimit.withLimit(newLimit);
    }

    @MemberSupport public int defaultNewLimit() {
        return hasLimit.getLimit();
    }

    @MemberSupport public String validateNewLimit(final int newLimit) {
        return newLimit >= 1 && newLimit <= CommandManager.MAX_LIMIT
                ? null
                : "Limit must be between 1 and " + CommandManager.MAX_LIMIT;
    }
}
