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
package org.apache.isis.applib.services.commanddto.conmap;

import lombok.val;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.commanddto.HasCommandDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.commanddto.processor.spi.CommandDtoProcessorService;
import org.apache.isis.applib.services.conmap.ContentMappingService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.lang.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 
 * @since 2.0 {@index}
 */
@Service
@Named("isis.applib.ContentMappingServiceForCommandDto")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("CommandDto")
public class ContentMappingServiceForCommandDto implements ContentMappingService {

    @Override @Nullable
    public Object map(final Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = Util.isSupported(CommandDto.class, acceptableMediaTypes);
        if(!supported) {
            return null;
        }

        return asProcessedDto(object);
    }

    @Nullable
    CommandDto asProcessedDto(final Object object) {
        val commandDto = asCommandDto(object);
        return asProcessedDto(object, commandDto);
    }

    @Nullable
    private CommandDto asCommandDto(final Object object) {
        if(object instanceof CommandDto) {
            return (CommandDto) object;
        }
        if(object instanceof HasCommandDto) {
            return ((HasCommandDto) object).getCommandDto();
        }
        return null;
    }

    @Nullable
    private CommandDto asProcessedDto(final Object domainObject, @Nullable CommandDto commandDto) {

        if(commandDto == null) {
            return null;
        }
        
        // global processors
        for (val commandDtoProcessorService : commandDtoProcessorServices) {
            commandDto = commandDtoProcessorService.process(domainObject, commandDto);
            if(commandDto == null) {
                // any processor could return null, effectively breaking the chain.
                return null;
            }
        }

        // specific processor for this specific member (action or property)
        val logicalMemberId = commandDto.getMember().getLogicalMemberIdentifier();
        final CommandDtoProcessor commandDtoProcessor =
                metaModelService.commandDtoProcessorFor(logicalMemberId);
        if (commandDtoProcessor == null) {
            return commandDto;
        }
        return commandDtoProcessor.process(commandDto);
    }


    @Inject MetaModelService metaModelService;
    @Inject List<CommandDtoProcessorService> commandDtoProcessorServices;

}
