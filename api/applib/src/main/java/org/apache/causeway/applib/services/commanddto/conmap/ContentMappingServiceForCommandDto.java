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
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.commanddto.HasCommandDto;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.commanddto.processor.spi.CommandDtoProcessorService;
import org.apache.causeway.applib.services.conmap.ContentMappingService;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.schema.cmd.v2.CommandDto;

/**
 * An implementation of {@link ContentMappingService}.
 * 
 * @since 2.0 {@index}
 */
@Service
@Named(ContentMappingServiceForCommandDto.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.EARLY)
@Qualifier("CommandDto")
public class ContentMappingServiceForCommandDto implements ContentMappingService {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".ContentMappingServiceForCommandDto";

    @Override @Nullable
    public Object map(final Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = isSupported(CommandDto.class, acceptableMediaTypes);
        if(!supported) {
            return null;
        }

        return asProcessedDto(object);
    }

    @Nullable
    CommandDto asProcessedDto(final Object object) {
        var commandDto = asCommandDto(object);
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
        for (var commandDtoProcessorService : commandDtoProcessorServices) {
            commandDto = commandDtoProcessorService.process(domainObject, commandDto);
            if(commandDto == null) {
                // any processor could return null, effectively breaking the chain.
                return null;
            }
        }

        // specific processor for this specific member (action or property)
        var logicalMemberId = commandDto.getMember().getLogicalMemberIdentifier();
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
