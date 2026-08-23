package demoapp.dom.domain.objects.DomainObjectLayout.paged;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

@Action()
@RequiredArgsConstructor
public class DomainObjectLayoutPagedPage_actionReturningObjects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutPagedPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutPagedEntity> act() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutPagedEntity> objectRepository;

}
