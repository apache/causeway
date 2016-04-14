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

import java.lang.reflect.Method;
import java.sql.Timestamp;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.schema.aim.v1.ActionInvocationMementoDto;

/**
 * Submit actions to be invoked in the background.
 * 
 * <p>
 * Example usage:
 * <pre>
 * public void submitInvoices() {
 *     for(Customer customer: customerRepository.findCustomersToInvoice()) {
 *         backgroundService.execute(customer).submitInvoice();
 *     }
 * }
 * 
 * &#64;javax.inject.Inject
 * private BackgroundService backgroundService;
 * </pre>
 */
public interface CommandMementoService {

    @Programmatic
    ActionInvocationMemento asActionInvocationMemento(Method m, Object domainObject, Object[] args);

    /**
     * @param command - must represent an action invocation (ie {@link Command#getTargetAction()} does not return {@link Command#TARGET_ACTION_FOR_EDIT}) (else throws exception).
     * @param currentUser - as provided by {@link UserService} (might change within an action if {@link SudoService} has been used).
     * @param timestamp - as obtained from clock (might want multiple events to all have the same clock, eg publishing muliple changed objects)
     * @param sequenceName - to create unique events per {@link Command#getTransactionId()}; see {@link EventMetadata#getId()}.
     */
    @Programmatic
    EventMetadata newEventMetadata(
            final Command command,
            final String currentUser,
            final Timestamp timestamp,
            final String sequenceName);

    /**
     * For {@link EventMetadata event}s representing an {@link org.apache.isis.applib.services.publish.EventType#ACTION_INVOCATION action invocation}, converts to an {@link ActionInvocationMementoDto}.
     *
     * <p>
     *     The additional information needed for the {@link ActionInvocationMementoDto DTO} (namely the action arguments and
     *     result) can be obtained from the thread-local in <tt>ActionInvocationFacet</tt>; this thread-local is reset for
     *     each action invoked (eg for outer action, or for each sub-action invoked via {@link WrapperFactory}).
     * </p>
     */
    @Programmatic
    ActionInvocationMementoDto asActionInvocationMementoDto(final EventMetadata metadata);

}
