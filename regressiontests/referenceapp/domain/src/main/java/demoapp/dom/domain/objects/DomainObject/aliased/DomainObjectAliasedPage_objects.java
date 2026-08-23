package demoapp.dom.domain.objects.DomainObject.aliased;

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
public class DomainObjectAliasedPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedPage page;

    @MemberSupport
    public List<? extends DomainObjectAliasedEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectAliasedEntity> objectRepository;

}
