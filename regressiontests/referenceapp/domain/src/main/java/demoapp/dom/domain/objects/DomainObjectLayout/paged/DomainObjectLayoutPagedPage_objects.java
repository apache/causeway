package demoapp.dom.domain.objects.DomainObjectLayout.paged;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutPagedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutPagedPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutPagedEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutPagedEntity> objectRepository;

}
