package demoapp.dom.domain.objects.DomainObjectLayout.bookmarking;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.aliased.DomainObjectAliased;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutBookmarkingPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutBookmarkingPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutBookmarking> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutBookmarking> objectRepository;

}
