package demoapp.dom.domain.objects.DomainObject.aliased;

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
public class DomainObjectAliasedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedPage page;

    @MemberSupport
    public List<? extends DomainObjectAliasedEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectAliasedEntity> objectRepository;

}
