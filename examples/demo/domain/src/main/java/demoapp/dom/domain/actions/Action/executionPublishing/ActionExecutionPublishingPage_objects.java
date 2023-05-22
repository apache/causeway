package demoapp.dom.domain.actions.Action.executionPublishing;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionExecutionPublishingPage_objects {

    @SuppressWarnings("unused")
    private final ActionExecutionPublishingPage page;

    @MemberSupport public List<? extends ActionExecutionPublishingEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionExecutionPublishingEntity> objectRepository;
}
//end::class[]
