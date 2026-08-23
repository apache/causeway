package demoapp.dom.domain.objects.DomainObject.editing;

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
public class DomainObjectEditingPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectEditingPage page;

    @MemberSupport
    public List<? extends DomainObjectEditingEntity> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectEditingEntity> objectRepository;

}
