package demoapp.dom.domain.objects.DomainObject.nature;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;
import demoapp.dom.domain.objects.DomainObject.nature.viewmodel.DomainObjectNatureViewModel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

//tag::class[]
@DomainObject(mixinMethod = "collection")                           // <.>
@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectNaturePage_viewModels {
    // ...
//end::class[]
    @SuppressWarnings("unused")
    private final DomainObjectNaturePage mixee;

//tag::class[]
    public List<DomainObjectNatureViewModel> collection() {   // <.>
        return entities.all().stream()
                .map(DomainObjectNatureViewModel::new)
                .collect(Collectors.toList());
    }
//end::class[]

    @Inject ValueHolderRepository<String, ? extends DomainObjectNatureEntity> entities;

//tag::class[]
}
//end::class[]
