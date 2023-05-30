package demoapp.dom.domain.objects.DomainObject.nature;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class DomainObjectNaturePage_entities {
    // ...
//end::class[]

    @SuppressWarnings("unused")
    private final DomainObjectNaturePage page;

//tag::class[]
    public List<? extends DomainObjectNatureEntity> coll() {
        return entities.all();
    }
//end::class[]

    @Inject ValueHolderRepository<String, ? extends DomainObjectNatureEntity> entities;
//tag::class[]
}
//end::class[]
