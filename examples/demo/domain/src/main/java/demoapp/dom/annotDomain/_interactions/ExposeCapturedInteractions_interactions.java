package demoapp.dom.annotDomain._interactions;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Collection
@RequiredArgsConstructor
public class ExposeCapturedInteractions_interactions {
    // ...
//end::class[]

    private final ExposeCapturedInteractions exposeCapturedInteractions;

//tag::class[]
    public List<InteractionDtoVm> coll() {
        val list = new LinkedList<InteractionDtoVm>();
        publisherServiceToCaptureInteractionsInMemory
                .streamInteractionDtos()
                .map(InteractionDtoVm::new)
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    PublisherServiceToCaptureInteractionsInMemory publisherServiceToCaptureInteractionsInMemory;
}
//end::class[]
