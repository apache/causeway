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

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.metamodel.MetaModelService5;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class ContentMappingServiceForCommandDto implements ContentMappingService {

    @Programmatic
    public Object map(Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = Util.isSupported(CommandDto.class, acceptableMediaTypes);
        if(!supported) {
            return null;
        }

        return asProcessedDto(object, metaModelService);
    }

    /**
     * Not part of the {@link ContentMappingService} API.
     */
    @Programmatic
    public CommandDto map(final CommandWithDto commandWithDto) {
        return asProcessedDto(commandWithDto, metaModelService);
    }

    static CommandDto asProcessedDto(
            final Object object,
            final MetaModelService5 metaModelService) {

        if (!(object instanceof CommandWithDto)) {
            return null;
        }
        final CommandWithDto commandWithDto = (CommandWithDto) object;
        return asProcessedDto(commandWithDto, metaModelService);
    }

    private static CommandDto asProcessedDto(
            CommandWithDto commandWithDto,
            final MetaModelService5 metaModelService) {
        final CommandDto commandDto = commandWithDto.asDto();
        /**
         * the timestamp field was only introduced in v1.4 of cmd.xsd, so there's no guarantee it will have been
         * populated.  We therefore copy the value in from CommandWithDto entity.
         */
        if(commandDto.getTimestamp() == null) {
            final Timestamp timestamp = commandWithDto.getTimestamp();
            commandDto.setTimestamp(JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));
        }
        final CommandDtoProcessor commandDtoProcessor =
                metaModelService.commandDtoProcessorFor(commandDto.getMember().getLogicalMemberIdentifier());
        if (commandDtoProcessor == null) {
            return commandDto;
        }
        return commandDtoProcessor.process(commandWithDto, commandDto);
    }

    @Inject
    MetaModelService5 metaModelService;

}
