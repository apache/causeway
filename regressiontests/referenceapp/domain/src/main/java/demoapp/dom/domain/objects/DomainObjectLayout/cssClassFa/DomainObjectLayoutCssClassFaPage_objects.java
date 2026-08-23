package demoapp.dom.domain.objects.DomainObjectLayout.cssClassFa;

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
public class DomainObjectLayoutCssClassFaPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutCssClassFaPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutCssClassFaEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutCssClassFaEntity> objectRepository;

}
