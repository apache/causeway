package demoapp.dom.domain.objects.DomainObject.bounded;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.autoComplete.DomainObjectAutoComplete;
import demoapp.dom.domain.objects.DomainObject.autoComplete.DomainObjectAutoCompleteVm;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectBoundingVm_objects {

    @SuppressWarnings("unused")
    private final DomainObjectBoundingVm mixee;

    @MemberSupport
    public List<? extends DomainObjectBounding> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectBounding> objectRepository;

}
