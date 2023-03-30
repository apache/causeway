package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;

//tag::class[]
@DomainObject(mixinMethod = "action")                               // <.>
@Action()
@RequiredArgsConstructor
public class DomainObjectMixinMethod_updateName {

    private final DomainObjectMixinMethod mixee;

    @MemberSupport
    public DomainObjectMixinMethod action(final String newName) {   // <.>
        mixee.setName(newName);
        return mixee;
    }
    public String default0Action() {
        return mixee.getName();
    }
}
//end::class[]
