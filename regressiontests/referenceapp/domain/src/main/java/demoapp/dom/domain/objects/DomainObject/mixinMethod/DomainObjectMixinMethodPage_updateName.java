package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@DomainObject(mixinMethod = "action")                   // <.>
@Action()
@ActionLayout()
@RequiredArgsConstructor
public class DomainObjectMixinMethodPage_updateName {
    // ...
//end::class[]
    private final DomainObjectMixinMethodPage page;

//tag::class[]
    public DomainObjectMixinMethodPage action(          // <.>
              final DomainObjectMixinMethodEntity object,
              final String newName
    ) {
        // ...
//end::class[]
        object.setName(newName);
        return page;
//tag::class[]
    }
//end::class[]
    public List<? extends DomainObjectMixinMethodEntity> choices0Action() {
        return objectRepository.all();
    }
    public String default1Action(final DomainObjectMixinMethodEntity object) {
        return Optional.ofNullable(object).map(DomainObjectMixinMethodEntity::getName).orElse(null);
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectMixinMethodEntity> objectRepository;

//tag::class[]
}
//end::class[]
