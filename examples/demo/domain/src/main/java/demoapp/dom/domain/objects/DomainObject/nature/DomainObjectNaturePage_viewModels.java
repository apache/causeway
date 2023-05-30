package demoapp.dom.domain.objects.DomainObject.nature;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;
import demoapp.dom.domain.objects.DomainObject.nature.viewmodel.DomainObjectNatureViewModel;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class DomainObjectNaturePage_viewModels {
    // ...
//end::class[]
    @SuppressWarnings("unused")
    private final DomainObjectNaturePage page;

//tag::class[]
    public List<DomainObjectNatureViewModel> coll() {
        return entities.all().stream()
                .map(DomainObjectNatureViewModel::new)  // <.>
                .collect(Collectors.toList());
    }
//end::class[]

    @Inject ValueHolderRepository<String, ? extends DomainObjectNatureEntity> entities;
//tag::class[]
}
//end::class[]
