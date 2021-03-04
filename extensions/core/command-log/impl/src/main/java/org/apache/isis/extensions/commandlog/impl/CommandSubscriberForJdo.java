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
package org.apache.isis.extensions.commandlog.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandLog.CommandCompletionHook")
@Order(OrderPrecedence.MIDPOINT) // after JdoPersistenceLifecycleService
@Qualifier("Jdo")
@Log4j2
@RequiredArgsConstructor
public class CommandSubscriberForJdo implements CommandSubscriber {

    @Inject final CommandJdoRepository commandJdoRepository;

    @Override
    public void onCompleted(Command command) {

        if(!command.isSystemStateChanged()) {
            return;
        }

        val existingCommandJdoIfAny =
                commandJdoRepository.findByUniqueId(command.getInteractionId());
        if(existingCommandJdoIfAny.isPresent()) {
            if(log.isDebugEnabled()) {
                // this isn't expected to happen ... we just log the fact if it does
                val existingCommandDto = existingCommandJdoIfAny.get().getCommandDto();

                String existingCommandDtoXml = JaxbUtil.toXml(existingCommandDto).presentElse("Dto to Xml failure");
                String commandDtoXml = JaxbUtil.toXml(command.getCommandDto()).presentElse("Dto to Xml failure");

                log.debug("existing: \n" + existingCommandDtoXml);
                log.debug("proposed: \n" + commandDtoXml);
            }
        } else {
            val commandJdo = new CommandJdo(command);
            val parent = command.getParent();
            val parentJdo =
                parent != null
                    ? commandJdoRepository
                        .findByUniqueId(parent.getInteractionId())
                        .orElse(null)
                    : null;
            commandJdo.setParent(parentJdo);
            commandJdoRepository.persist(commandJdo);
        }


    }

}
