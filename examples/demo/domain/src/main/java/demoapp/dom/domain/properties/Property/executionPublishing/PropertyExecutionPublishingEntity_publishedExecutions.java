package demoapp.dom.domain.properties.Property.executionPublishing;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyExecutionPublishingEntity_publishedExecutions {

    @SuppressWarnings("unused")
    private final PropertyExecutionPublishingEntity entity;

    @MemberSupport public List<? extends ExecutionLogEntry> coll() {
        return executionLogEntryRepository.findRecentByTarget(bookmarkService.bookmarkForElseFail(entity));
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
    @Inject BookmarkService bookmarkService;
}
//end::class[]
