package demoapp.dom.domain.objects.DomainObjectLayout.cssClass;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutCssClassPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutCssClassPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutCssClassEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutCssClassEntity> objectRepository;

}
