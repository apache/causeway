package demoapp.dom.domain.objects.DomainObject.editing;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingVm;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectEditingVm_objects {

    @SuppressWarnings("unused")
    private final DomainObjectEditingVm mixee;

    @MemberSupport
    public List<? extends DomainObjectEditing> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEditing> objectRepository;

}
