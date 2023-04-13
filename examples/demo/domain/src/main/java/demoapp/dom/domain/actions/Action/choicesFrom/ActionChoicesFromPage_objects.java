package demoapp.dom.domain.actions.Action.choicesFrom;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.named.DomainObjectLayoutNamedPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionChoicesFromPage_objects {

    @SuppressWarnings("unused")
    private final ActionChoicesFromPage page;

    @MemberSupport public List<? extends ActionChoicesFrom> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionChoicesFrom> objectRepository;
}
//end::class[]
