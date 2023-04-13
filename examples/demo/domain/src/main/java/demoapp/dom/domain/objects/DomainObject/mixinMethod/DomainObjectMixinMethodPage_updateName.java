package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

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
              final DomainObjectMixinMethod object,
              final String newName
    ) {
        // ...
//end::class[]
        object.setName(newName);
        return page;
//tag::class[]
    }
//end::class[]
    public List<? extends DomainObjectMixinMethod> choices0Action() {
        return objectRepository.all();
    }
    public String default1Action(final DomainObjectMixinMethod object) {
        return Optional.ofNullable(object).map(DomainObjectMixinMethod::getName).orElse(null);
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectMixinMethod> objectRepository;

//tag::class[]
}
//end::class[]
