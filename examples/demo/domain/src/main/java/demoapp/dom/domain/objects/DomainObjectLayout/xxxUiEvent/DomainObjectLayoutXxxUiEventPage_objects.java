package demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutXxxUiEventPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutXxxUiEventPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutXxxUiEventEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutXxxUiEventEntity> objectRepository;

}
