package demoapp.dom.domain.objects.DomainObjectLayout.paged;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.describedAs.DomainObjectLayoutDescribedAsPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutPagedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutPagedPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutPaged> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutPaged> objectRepository;

}
