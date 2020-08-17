package demoapp.dom.annotDomain.DomainObject.publishing.spiimpl;

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
public class PublisherServiceSpiForDomainObject implements PublisherService {

    private final List<ChangesDto> publishedObjects = new ArrayList<ChangesDto>();

    @Override
    public void publish(Interaction.Execution<?, ?> execution) {
    }

    @Override
    public void publish(PublishedObjects publishedObjects) {
        final ChangesDto dto = publishedObjects.getDto();
        this.publishedObjects.add(dto);
    }

    public Stream<ChangesDto> streamPublishedObjects() {
        return publishedObjects.stream();
    }

    public void clear() {
        publishedObjects.clear();
    }

}
