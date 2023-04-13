package demoapp.dom.domain.objects.DomainObjectLayout.cssClassFa;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObjectLayout.cssClass.DomainObjectLayoutCssClassPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectLayoutCssClassFaPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutCssClassFaPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutCssClassFa> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutCssClassFa> objectRepository;

}
