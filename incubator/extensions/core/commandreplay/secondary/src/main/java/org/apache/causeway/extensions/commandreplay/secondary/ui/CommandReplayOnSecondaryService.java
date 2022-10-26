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
package org.apache.causeway.extensions.commandreplay.secondary.ui;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandreplay.secondary.CausewayModuleExtCommandReplaySecondary;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.VIEW
)
@Named(CausewayModuleExtCommandReplaySecondary.NAMESPACE + ".CommandReplayOnSecondaryService")
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor
//@Log4j2
public class CommandReplayOnSecondaryService {

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject JaxbService jaxbService;

    public static abstract class ActionDomainEvent<T> extends CausewayModuleExtCommandReplaySecondary.ActionDomainEvent<T> { }

    @Action(domainEvent = findMostRecentReplayed.ActionDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-bath", sequence="60.1")
    public class findMostRecentReplayed{

        public class ActionDomainEvent extends CommandReplayOnSecondaryService.ActionDomainEvent<findMostRecentReplayed> { }

        @MemberSupport public CommandLogEntry act() {
            return commandLogEntryRepository.findMostRecentReplayed().orElse(null);
        }
    }


    @Action(
        domainEvent = uploadCommands.ActionDomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @ActionLayout(cssClassFa = "fa-upload", sequence="60.2")
    public class uploadCommands{

        public class ActionDomainEvent extends CommandReplayOnSecondaryService.ActionDomainEvent<uploadCommands> { }

        @MemberSupport public void act(final Clob commandsDtoAsXml) {
            val chars = commandsDtoAsXml.getChars();
            List<CommandDto> commandDtoList;

            try {
                val commandsDto = jaxbService.fromXml(CommandsDto.class, chars.toString());
                commandDtoList = commandsDto.getCommandDto();

            } catch(Exception ex) {
                val commandDto = jaxbService.fromXml(CommandDto.class, chars.toString());
                commandDtoList = Collections.singletonList(commandDto);
            }

            for (final CommandDto commandDto : commandDtoList) {
                commandLogEntryRepository.saveForReplay(commandDto);
            }
        }

    }

}

