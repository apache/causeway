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
package org.apache.isis.extensions.commandreplay.primary.spiimpl;

import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.commanddto.processor.spi.CommandDtoProcessorService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

/**
 * @since 2.0 {@index}
 */

/**
 * Uses the SPI infrastructure to copy over standard properties from {@link Command} to {@link CommandDto}.
 */
@Service
@Named("isis.ext.commandReplayPrimary.CaptureResultOfCommand")
// specify quite a high priority since custom processors will probably want to run after this one
// (but can choose to run before if they wish)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class CaptureResultOfCommand implements CommandDtoProcessorService {

    @Override
    public CommandDto process(final Object domainObject, CommandDto commandDto) {

        if (!(domainObject instanceof CommandModel)) {
            return commandDto;
        }

        val commandModel = (CommandModel) domainObject;
        if(commandDto == null) {
            commandDto = commandModel.getCommandDto();
        }

        final Bookmark result = commandModel.getResult();
        CommandDtoUtils.setUserData(commandDto, UserDataKeys.RESULT, result);

        // knowing whether there was an exception is on the primary is
        // used to determine whether to continue when replayed on the
        // secondary if an exception occurs there also
        CommandDtoUtils.setUserData(commandDto,
                UserDataKeys.EXCEPTION,
                commandModel.getException());

        val timings = CommandDtoUtils.timingsFor(commandDto);
        timings.setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(commandModel.getStartedAt()));
        timings.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(commandModel.getCompletedAt()));

        return commandDto;
    }
}
