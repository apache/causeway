package demoapp.dom.annotDomain._changes;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.chg.v2.ChangesDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Collection
@RequiredArgsConstructor
public class ExposeCapturedChanges_changes {
    // ...
//end::class[]

    private final ExposeCapturedChanges exposeCapturedChanges;

//tag::class[]
    public List<ChangesDto> coll() {
        val list = new LinkedList<ChangesDto>();
        publisherServiceToCaptureChangesInMemory
                .streamPublishedObjects()
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    PublisherServiceToCaptureChangesInMemory publisherServiceToCaptureChangesInMemory;
}
//end::class[]
