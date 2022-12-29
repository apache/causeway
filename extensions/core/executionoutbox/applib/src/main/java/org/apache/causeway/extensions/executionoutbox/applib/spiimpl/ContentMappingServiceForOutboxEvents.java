/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionoutbox.applib.spiimpl;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.conmap.ContentMappingService;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.restapi.OutboxEvents;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;

import lombok.val;

/**
 * Implementation of {@link ContentMappingService} that is responsible for serializing a list of
 * @since 2.0 {@index}
 */
@Service
public class ContentMappingServiceForOutboxEvents implements ContentMappingService {

    @Override
    public Object map(final Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = isSupported(InteractionsDto.class, acceptableMediaTypes);
        if(!supported) {
            return null;
        }

        return map(object);
    }

    private Object map(final Object object) {
        if (!(object instanceof OutboxEvents)) {
            return null;
        }

        val outboxEvents = (OutboxEvents) object;

        val dto = new InteractionsDto();
        outboxEvents.getExecutions().stream()
                .map(ExecutionOutboxEntry::getInteractionDto)
                .filter(Objects::nonNull)
                .forEach(x -> dto.getInteractionDto().add(x));

        return dto;
    }

}
