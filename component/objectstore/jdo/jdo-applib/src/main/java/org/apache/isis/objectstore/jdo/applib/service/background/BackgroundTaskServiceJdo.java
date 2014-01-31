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
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundTaskService;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;

@Named("Background Tasks")
public class BackgroundTaskServiceJdo extends AbstractService implements BackgroundTaskService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskServiceJdo.class);

    @Programmatic
    @Override
    public void execute(final ActionInvocationMemento aim, final ReifiableAction reifiableAction) {
        
        final UUID transactionId = reifiableAction.getTransactionId();
        Integer sequence = reifiableAction.next("backgroundTaskServiceSequence");

        final BackgroundTaskJdo backgroundTask = newTransientInstance(BackgroundTaskJdo.class);

        backgroundTask.setActionIdentifier(aim.getActionId());
        backgroundTask.setTimestamp(Clock.getTimeAsJavaSqlTimestamp());
        backgroundTask.setMemento(aim.asMementoString());
        backgroundTask.setUser(aim.getUser());
        backgroundTask.setTargetStr(aim.getTarget().toString());
        backgroundTask.setTransactionId(transactionId);
        backgroundTask.setSequence(sequence);
        
        reifiableAction.setReify(true);
        
        persist(backgroundTask);
    }

}
