package demoapp.dom.domain.actions.ActionLayout.hidden;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionLayoutHiddenPage_objects {

    @SuppressWarnings("unused")
    private final ActionLayoutHiddenPage page;

    @MemberSupport public List<? extends ActionLayoutHiddenEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionLayoutHiddenEntity> objectRepository;
}
//end::class[]
