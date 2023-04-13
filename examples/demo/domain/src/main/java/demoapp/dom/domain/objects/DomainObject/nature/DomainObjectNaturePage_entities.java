package demoapp.dom.domain.objects.DomainObject.nature;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;

@Collection()
@RequiredArgsConstructor
public class DomainObjectNaturePage_entities {

    @SuppressWarnings("unused")
    private final DomainObjectNaturePage page;

    public List<? extends DomainObjectNatureEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectNatureEntity> objectRepository;
}
