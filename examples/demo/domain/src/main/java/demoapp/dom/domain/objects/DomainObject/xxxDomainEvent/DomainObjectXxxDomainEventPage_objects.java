package demoapp.dom.domain.objects.DomainObject.xxxDomainEvent;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.mixinMethod.DomainObjectMixinMethod;
import demoapp.dom.domain.objects.DomainObject.mixinMethod.DomainObjectMixinMethodPage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectXxxDomainEventPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectXxxDomainEventPage mixee;

    public List<? extends DomainObjectXxxDomainEvent> coll() {   // <.>
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectXxxDomainEvent> objectRepository;
}
