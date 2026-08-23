package demoapp.dom.domain.objects.DomainObject.bounded;

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
public class DomainObjectBoundingPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectBoundingPage page;

    @MemberSupport
    public List<? extends DomainObjectBoundingEntity> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectBoundingEntity> objectRepository;

}
