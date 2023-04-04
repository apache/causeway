package demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.tabledec.DomainObjectLayoutTableDecoratorPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutXxxUiEventPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutXxxUiEventPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutXxxUiEvent> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutXxxUiEvent> objectRepository;

}
