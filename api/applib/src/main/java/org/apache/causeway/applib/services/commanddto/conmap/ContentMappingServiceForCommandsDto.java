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
package org.apache.causeway.applib.services.commanddto.conmap;

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.conmap.ContentMappingService;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;

/**
 *
 * @since 2.0 {@index}
 */
@Service
@Named(ContentMappingServiceForCommandsDto.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.EARLY)
@Qualifier("CommandsDto")
public class ContentMappingServiceForCommandsDto implements ContentMappingService {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".ContentMappingServiceForCommandsDto";

    @Override
    public Object map(final Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = isSupported(CommandsDto.class, acceptableMediaTypes);
        if(!supported) {
            return null;
        }

        return map(object);
    }

    /**
     * Not part of the {@link ContentMappingService} API.
     */
    public CommandsDto map(final Object object) {
        if(object instanceof CommandsDto) {
            return ((CommandsDto) object);
        }

        CommandDto commandDto = asDto(object);
        if(commandDto != null) {
            final CommandsDto commandsDto = new CommandsDto();
            commandsDto.getCommandDto().add(commandDto);
            return commandsDto;
        }

        if (object instanceof List) {
            final List<?> list = (List<?>) object;
            final CommandsDto commandsDto = new CommandsDto();
            for (final Object obj : list) {
                final CommandDto objAsCommandDto = asDto(obj);
                if(objAsCommandDto != null) {
                    commandsDto.getCommandDto().add(objAsCommandDto);
                } else {
                    // simply ignore.
                    // this is the means by which we can avoid replicating commands.
                }
            }
            return commandsDto;
        }

        // else
        return new CommandsDto();
    }

    private CommandDto asDto(final Object object) {
        return contentMappingServiceForCommandDto.asProcessedDto(object);
    }

    @Inject
    ContentMappingServiceForCommandDto contentMappingServiceForCommandDto;

}
