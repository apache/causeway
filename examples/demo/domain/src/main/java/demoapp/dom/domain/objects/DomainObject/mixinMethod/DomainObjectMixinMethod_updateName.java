package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;

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
