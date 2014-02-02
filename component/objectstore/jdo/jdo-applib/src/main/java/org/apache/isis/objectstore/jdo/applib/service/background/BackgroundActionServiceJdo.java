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
import org.apache.isis.applib.services.background.BackgroundActionService;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction.Nature;
import org.apache.isis.objectstore.jdo.applib.service.reifiableaction.ReifiableActionJdo;

@Named("Background Actions")
public class BackgroundActionServiceJdo extends AbstractService implements BackgroundActionService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundActionServiceJdo.class);
    
    @Programmatic
    @Override
    public void schedule(
            final ActionInvocationMemento aim, 
            final ReifiableAction parentAction, 
            final String targetClassName, 
            final String targetActionName, 
            final String targetArgs) {
        
        final UUID transactionId = UUID.randomUUID();
        final String user = parentAction.getUser();

        final ReifiableActionJdo backgroundAction = newTransientInstance(ReifiableActionJdo.class);

        backgroundAction.setParent(parentAction);
        
        backgroundAction.setTransactionId(transactionId);

        backgroundAction.setUser(user);
        backgroundAction.setTimestamp(Clock.getTimeAsJavaSqlTimestamp());

        backgroundAction.setNature(Nature.BACKGROUND);

        backgroundAction.setTargetClass(targetClassName);
        backgroundAction.setTargetAction(targetActionName);
        backgroundAction.setTargetStr(aim.getTarget().toString());
        backgroundAction.setActionIdentifier(aim.getActionId());

        backgroundAction.setArguments(targetArgs);
        backgroundAction.setMemento(aim.asMementoString());
        
        parentAction.setReify(true);
        
        persist(backgroundAction);
    }

}
