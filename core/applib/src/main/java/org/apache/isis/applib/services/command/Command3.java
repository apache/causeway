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

/**
 * An extension to {@link Command} that makes the
 * relationship with {@link org.apache.isis.applib.services.eventbus.ActionDomainEvent} bi-directional.
 */
public interface Command3 extends Command2 {

    /**
     * @deprecated - use {@link Interaction#peekDomainEvent()} instead.
     */
    @Deprecated
    @Programmatic
    ActionDomainEvent<?> peekActionDomainEvent();

    /**
     * @deprecated - use {@link Interaction#pushDomainEvent(org.apache.isis.applib.services.eventbus.AbstractDomainEvent)} instead.
     */
    @Deprecated
    @Programmatic
    void pushActionDomainEvent(ActionDomainEvent<?> event);

    /**
     * @deprecated - use {@link Interaction#popDomainEvent()} instead.
     */
    @Deprecated
    @Programmatic
    ActionDomainEvent<?> popActionDomainEvent();

    /**
     * @deprecated - use {@link Interaction#getDomainEvents()} and {@link Interaction#clearDomainEvents()} instead.
     */
    @Deprecated
    @Programmatic
    List<ActionDomainEvent<?>> flushActionDomainEvents();
}
