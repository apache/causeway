package demoapp.dom.domain.actions.Action.choicesFrom;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionChoicesFromPage_objects {

    @SuppressWarnings("unused")
    private final ActionChoicesFromPage page;

    @MemberSupport public List<? extends ActionChoicesFromEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionChoicesFromEntity> objectRepository;
}
//end::class[]
