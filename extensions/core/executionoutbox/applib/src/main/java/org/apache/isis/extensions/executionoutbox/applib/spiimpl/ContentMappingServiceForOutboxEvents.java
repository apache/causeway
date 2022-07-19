package org.apache.isis.extensions.executionoutbox.applib.spiimpl;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.conmap.ContentMappingService;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.restapi.OutboxEvents;
import org.apache.isis.schema.ixn.v2.InteractionsDto;

import lombok.val;

@Service
public class ContentMappingServiceForOutboxEvents implements ContentMappingService {

    @Override
    public Object map(final Object object, final List<MediaType> acceptableMediaTypes) {
        final boolean supported = Util.isSupported(InteractionsDto.class, acceptableMediaTypes);
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
