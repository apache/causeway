/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.command;

import java.util.List;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;

/**
 * @deprecated - the extensions to {@link Command} in this interface have been replaced by functionality in {@link InteractionContext}.
 */
@Deprecated
public interface Command3 extends Command2 {

    /**
     * @deprecated - use {@link Interaction#getCurrentExecution()}, {@link Interaction#getPriorExecution()}  and {@link Interaction#getExecutions()} instead.
     */
    @Deprecated
    @Programmatic
    ActionDomainEvent<?> peekActionDomainEvent();

    /**
     * @deprecated - replaced by equivalent functionality in {@link Interaction}.
     */
    @Deprecated
    @Programmatic
    void pushActionDomainEvent(ActionDomainEvent<?> event);

    /**
     * @deprecated - replaced by equivalent functionality in {@link Interaction}.
     */
    @Deprecated
    @Programmatic
    ActionDomainEvent<?> popActionDomainEvent();

    /**
     * @deprecated - use {@link Interaction#getCurrentExecution()}, {@link Interaction#getPriorExecution()}  and {@link Interaction#getExecutions()} instead.
     */
    @Deprecated
    @Programmatic
    List<ActionDomainEvent<?>> flushActionDomainEvents();
}
