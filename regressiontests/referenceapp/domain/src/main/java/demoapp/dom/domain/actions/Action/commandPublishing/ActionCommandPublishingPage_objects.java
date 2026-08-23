package demoapp.dom.domain.actions.Action.commandPublishing;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionCommandPublishingPage_objects {

    @SuppressWarnings("unused")
    private final ActionCommandPublishingPage page;

    @MemberSupport public List<? extends ActionCommandPublishingEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionCommandPublishingEntity> objectRepository;
}
//end::class[]
