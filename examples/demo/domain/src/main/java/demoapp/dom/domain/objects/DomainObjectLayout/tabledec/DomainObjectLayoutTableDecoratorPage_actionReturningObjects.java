package demoapp.dom.domain.objects.DomainObjectLayout.tabledec;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.paged.DomainObjectLayoutPaged;
import demoapp.dom.domain.objects.DomainObjectLayout.paged.DomainObjectLayoutPagedPage;
import lombok.RequiredArgsConstructor;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;

import jakarta.inject.Inject;
import java.util.List;

@Action()
@RequiredArgsConstructor
public class DomainObjectLayoutTableDecoratorPage_actionReturningObjects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutTableDecoratorPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutTableDecorator> act() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutTableDecorator> objectRepository;

}
