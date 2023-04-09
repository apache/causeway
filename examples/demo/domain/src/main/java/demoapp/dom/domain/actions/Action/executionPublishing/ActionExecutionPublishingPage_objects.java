package demoapp.dom.domain.actions.Action.executionPublishing;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionExecutionPublishingPage_objects {

    @SuppressWarnings("unused")
    private final ActionExecutionPublishingPage page;

    @MemberSupport public List<? extends ActionExecutionPublishing> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionExecutionPublishing> objectRepository;
}
//end::class[]
