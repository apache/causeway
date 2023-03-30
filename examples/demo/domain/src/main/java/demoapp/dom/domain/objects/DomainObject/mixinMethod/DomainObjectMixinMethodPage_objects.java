package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

//tag::class[]
@DomainObject(mixinMethod = "collection")                           // <.>
@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectMixinMethodPage_objects {
    // ...
//end::class[]
    @SuppressWarnings("unused")
    private final DomainObjectMixinMethodPage mixee;

//tag::class[]
    public List<? extends DomainObjectMixinMethod> collection() {   // <.>
        // ...
//end::class[]
        return objectRepository.all();
//tag::class[]
    }
//end::class[]

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectMixinMethod> objectRepository;

//tag::class[]
}
//end::class[]
