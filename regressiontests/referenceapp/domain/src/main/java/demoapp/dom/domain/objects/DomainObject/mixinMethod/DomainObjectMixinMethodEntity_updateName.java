package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

//tag::class[]
@DomainObject(mixinMethod = "action")                                           // <.>
@Action()
@RequiredArgsConstructor
public class DomainObjectMixinMethodEntity_updateName {

    private final DomainObjectMixinMethodEntity mixee;

    @MemberSupport
    public DomainObjectMixinMethodEntity action(final String newName) {         // <.>
        mixee.setName(newName);
        return mixee;
    }
    public String default0Action() {
        return mixee.getName();
    }
}
//end::class[]
