package demoapp.dom.domain.objects.DomainObjectLayout.bookmarking;

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
public class DomainObjectLayoutBookmarkingPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutBookmarkingPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutBookmarkingEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutBookmarkingEntity> objectRepository;

}
