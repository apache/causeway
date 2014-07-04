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
package org.apache.isis.objectstore.jdo.applib.service.background;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Command.ExecuteIn;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;

/**
 * Persists a {@link ActionInvocationMemento memento-ized} action such that it can be executed asynchronously,
 * for example through a Quartz scheduler (using
 * {@link org.apache.isis.objectstore.jdo.service.BackgroundCommandExecutionFromBackgroundCommandServiceJdo}).
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  This class is implemented in the
 * <tt>o.a.i.module:isis-module-command-jdo</tt> module.  If that module is included in the classpath, it this means
 * that this service is automatically registered; no further configuration is required.
 *
 * <p>
 * (That said, do note that other services in the <tt>o.a.i.module:isis-module-command-jdo</tt> do require explicit
 * registration as services, eg in <tt>isis.properties</tt>).
 */
@DomainService
public class BackgroundCommandServiceJdo extends AbstractService implements BackgroundCommandService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundCommandServiceJdo.class);
    
    @Programmatic
    @Override
    public void schedule(
            final ActionInvocationMemento aim, 
            final Command parentCommand, 
            final String targetClassName, 
            final String targetActionName, 
            final String targetArgs) {
        
        final UUID transactionId = UUID.randomUUID();
        final String user = parentCommand.getUser();

        final CommandJdo backgroundCommand = newTransientInstance(CommandJdo.class);

        backgroundCommand.setParent(parentCommand);
        
        backgroundCommand.setTransactionId(transactionId);

        backgroundCommand.setUser(user);
        backgroundCommand.setTimestamp(Clock.getTimeAsJavaSqlTimestamp());

        backgroundCommand.setExecuteIn(ExecuteIn.BACKGROUND);

        backgroundCommand.setTargetClass(targetClassName);
        backgroundCommand.setTargetAction(targetActionName);
        backgroundCommand.setTargetStr(aim.getTarget().toString());
        backgroundCommand.setMemberIdentifier(aim.getActionId());

        backgroundCommand.setArguments(targetArgs);
        backgroundCommand.setMemento(aim.asMementoString());
        
        parentCommand.setPersistHint(true);
        
        persist(backgroundCommand);
    }

}
