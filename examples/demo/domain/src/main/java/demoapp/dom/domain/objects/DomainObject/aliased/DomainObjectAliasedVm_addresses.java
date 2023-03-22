package demoapp.dom.domain.objects.DomainObject.aliased;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

@Collection()
@CollectionLayout(paged = 3)
@RequiredArgsConstructor
public class DomainObjectAliasedVm_addresses {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedVm mixee;

    @MemberSupport
    public List<? extends Address> coll() {
        return addressEntities.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends Address> addressEntities;

}
