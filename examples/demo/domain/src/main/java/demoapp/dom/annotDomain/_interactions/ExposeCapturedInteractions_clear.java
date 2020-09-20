package demoapp.dom.annotDomain._interactions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "interactions"
)
@RequiredArgsConstructor
public class ExposeCapturedInteractions_clear {
    // ...
//end::class[]

    private final ExposeCapturedInteractions exposeCapturedInteractions;

//tag::class[]
    public ExposeCapturedInteractions act() {
        publisherServiceToCaptureInteractionsInMemory.clear();
        return exposeCapturedInteractions;
    }

    @Inject
    PublisherServiceToCaptureInteractionsInMemory publisherServiceToCaptureInteractionsInMemory;
}
//end::class[]
