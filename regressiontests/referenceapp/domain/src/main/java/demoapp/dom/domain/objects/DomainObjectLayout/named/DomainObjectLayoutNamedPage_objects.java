package demoapp.dom.domain.objects.DomainObjectLayout.named;

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
public class DomainObjectLayoutNamedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutNamedPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutNamedEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutNamedEntity> objectRepository;

}
