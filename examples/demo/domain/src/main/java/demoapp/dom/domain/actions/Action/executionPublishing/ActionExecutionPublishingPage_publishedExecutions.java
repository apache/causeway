package demoapp.dom.domain.actions.Action.executionPublishing;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionExecutionPublishingPage_publishedExecutions {

    @SuppressWarnings("unused")
    private final ActionExecutionPublishingPage page;

    @MemberSupport public List<? extends ExecutionLogEntry> coll() {
        return executionLogEntryRepository
                .findMostRecent()
                .stream()
                /* display those log entries that are either associated with the (mixee) page
                 * or the underlying ActionExecutionPublishingEntity as show-cased via a nested WrapperFactory call*/
                .filter(execEntry->
                    execEntry.getTargetLogicalTypeName().equals("demo.ActionExecutionPublishingPage")
                        || execEntry.getTargetLogicalTypeName().equals("demo.ActionExecutionPublishingEntity"))
                .collect(Collectors.toList());
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository; // <.>
    @Inject BookmarkService bookmarkService;
}
//end::class[]
