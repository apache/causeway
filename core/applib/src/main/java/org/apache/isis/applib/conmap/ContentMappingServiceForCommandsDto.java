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
package org.apache.isis.applib.conmap;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.conmap.ContentMappingService;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.CommandsDto;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class ContentMappingServiceForCommandsDto implements ContentMappingService {

    @Override
    public Object map(Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = Util.isSupported(CommandsDto.class, acceptableMediaTypes);
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
