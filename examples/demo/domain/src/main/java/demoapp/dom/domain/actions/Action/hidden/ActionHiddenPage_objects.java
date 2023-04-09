package demoapp.dom.domain.actions.Action.hidden;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishing;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionHiddenPage_objects {

    @SuppressWarnings("unused")
    private final ActionHiddenPage page;

    @MemberSupport public List<? extends ActionHidden> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionHidden> objectRepository;
}
//end::class[]
