package demoapp.dom.domain.objects.DomainObject.aliased;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectAliasedVm_objects {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedVm mixee;

    @MemberSupport
    public List<? extends DomainObjectAliased> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectAliased> objectRepository;

}
