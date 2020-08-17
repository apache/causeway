package demoapp.dom.annotDomain.Property.publishing.spiimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;

@Service
public class PublisherServiceSpiForProperties implements PublisherService {

    private final List<InteractionDto> executions = new ArrayList<>();

    @Override
    public void publish(Interaction.Execution<?, ?> execution) {

        final InteractionDto dto =
                InteractionDtoUtils.newInteractionDto(execution, InteractionDtoUtils.Strategy.DEEP);

        executions.add(dto);
    }

    @Override
    public void publish(PublishedObjects publishedObjects) {
    }

    public Stream<InteractionDto> streamInteractionDtos() {
        return executions.stream();
    }

    public void clear() {
        executions.clear();
    }

}
