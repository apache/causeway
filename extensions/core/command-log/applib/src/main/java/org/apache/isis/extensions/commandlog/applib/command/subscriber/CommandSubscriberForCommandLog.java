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
package org.apache.isis.extensions.commandlog.applib.command.subscriber;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named(IsisModuleExtCommandLogApplib.NAMESPACE + ".CommandSubscriberForCommandLog")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT) // after JdoPersistenceLifecycleService
@Qualifier("Jdo")
@RequiredArgsConstructor
@Log4j2
public class CommandSubscriberForCommandLog implements CommandSubscriber {

    @Inject final ICommandLogRepository<? extends CommandLog> commandLogRepository;

    @Override
    public void onCompleted(final Command command) {

        if(!command.isSystemStateChanged()) {
            return;
        }

        val existingCommandJdoIfAny =
                commandLogRepository.findByInteractionId(command.getInteractionId());
        if(existingCommandJdoIfAny.isPresent()) {
            if(log.isDebugEnabled()) {
                // this isn't expected to happen ... we just log the fact if it does
                val existingCommandDto = existingCommandJdoIfAny.get().getCommandDto();

                val existingCommandDtoXml = JaxbUtil.toXml(existingCommandDto)
                        .getValue().orElse("Dto to Xml failure");
                val commandDtoXml = JaxbUtil.toXml(command.getCommandDto())
                        .getValue().orElse("Dto to Xml failure");

                log.debug("existing: \n{}", existingCommandDtoXml);
                log.debug("proposed: \n{}", commandDtoXml);
            }
        } else {
            val commandLogInstance = commandLogRepository.createCommandLog(command);
            val parent = command.getParent();
            val parentJdo =
                parent != null
                    ? commandLogRepository
                        .findByInteractionId(parent.getInteractionId())
                        .orElse(null)
                    : null;
            commandLogInstance.setParent(parentJdo);
            commandLogRepository.persist(_Casts.uncheckedCast(commandLogInstance));
        }


    }

}
