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
package org.apache.isis.applib.services.iactn;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;

/**
 * @since 1.x {@index}
 */
public class ActionInvocation
extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

    @Getter
    private final List<Object> args;

    public ActionInvocation(
            final Interaction interaction,
            final Identifier memberId,
            final Object target,
            final List<Object> args,
            final String targetMember,
            final String targetClass) {
        super(interaction, InteractionType.ACTION_INVOCATION, memberId, target, targetMember, targetClass);
        this.args = args;
    }
    // ...
}
