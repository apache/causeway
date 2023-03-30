package demoapp.dom.domain.objects.DomainObject.nature;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

@Collection()
@RequiredArgsConstructor
public class DomainObjectNaturePage_entities {

    @SuppressWarnings("unused")
    private final DomainObjectNaturePage mixee;

    public List<? extends DomainObjectNatureEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectNatureEntity> objectRepository;
}
