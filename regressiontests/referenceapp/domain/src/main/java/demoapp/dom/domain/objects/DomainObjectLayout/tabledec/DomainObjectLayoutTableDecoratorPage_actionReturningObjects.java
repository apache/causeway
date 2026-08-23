package demoapp.dom.domain.objects.DomainObjectLayout.tabledec;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

@Action()
@RequiredArgsConstructor
public class DomainObjectLayoutTableDecoratorPage_actionReturningObjects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutTableDecoratorPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutTableDecoratorEntity> act() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutTableDecoratorEntity> objectRepository;

}
