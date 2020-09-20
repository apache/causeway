package demoapp.dom.annotDomain._changes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.schema.chg.v2.ChangesDto;

import lombok.val;

//tag::class[]
@Service
public class PublisherServiceToCaptureChangesInMemory implements PublisherService {

    private final List<ChangesDto> publishedObjects = new ArrayList<>();

    @Override
    public void publish(
            PublishedObjects publishedObjects       // <.>
    ) {
        val dto = publishedObjects.getDto();
        this.publishedObjects.add(dto);
    }
    // ...
//end::class[]

//tag::demo[]
    public Stream<ChangesDto> streamPublishedObjects() {
        return publishedObjects.stream();
    }

    public void clear() {
        publishedObjects.clear();
    }
//end::demo[]

    @Override
    public void publish(Interaction.Execution<?, ?> execution) {
    }

//tag::class[]
}
//end::class[]
