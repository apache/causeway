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
package org.apache.isis.objectstore.jdo.applib.service.command;

import java.util.UUID;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.command.Command;


/**
 * This service contributes a <tt>command</tt> action to any (non-command) implementation of
 * {@link org.apache.isis.applib.services.HasTransactionId}; that is: audit entries, and published events.  Thus, it
 * is possible to navigate from the effect back to the cause.
 *
 * <p>
 * Because this service influences the UI, it must be explicitly registered as a service
 * (eg using <tt>isis.properties</tt>).
 */
public class CommandServiceJdoContributions extends AbstractFactoryAndRepository {

    @NotInServiceMenu
    @NotContributed(As.ASSOCIATION) // ie contributed as an action
    @MemberOrder(name="transactionId", sequence="1")
    public CommandJdo command(final HasTransactionId hasTransactionId) {
        return commandServiceRepository.findByTransactionId(hasTransactionId.getTransactionId());
    }
    /**
     * Hide if the contributee is a {@link Command}, because {@link Command}s already have a
     * {@link Command#getParent() parent} property.
     */
    public boolean hideCommand(final HasTransactionId hasTransactionId) {
        return (hasTransactionId instanceof Command);
    }
    public String disableCommand(final HasTransactionId hasTransactionId) {
        if(hasTransactionId == null) {
            return "No transaction Id";
        }
        final UUID transactionId = hasTransactionId.getTransactionId();
        final boolean command = commandServiceRepository.findByTransactionId(transactionId) == null;
        return command? "No command found for transaction Id": null;
    }


    // //////////////////////////////////////

    
    @javax.inject.Inject
    private CommandServiceJdoRepository commandServiceRepository;


}
