package demoapp.dom.domain.objects.DomainObjectLayout.cssClass;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.DomainObjectLayoutBookmarkingPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutCssClassPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutCssClassPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutCssClass> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutCssClass> objectRepository;

}
