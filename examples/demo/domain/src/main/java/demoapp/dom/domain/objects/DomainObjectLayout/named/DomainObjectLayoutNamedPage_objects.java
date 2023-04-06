package demoapp.dom.domain.objects.DomainObjectLayout.named;

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
public class DomainObjectLayoutNamedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutNamedPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutNamed> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutNamed> objectRepository;

}
