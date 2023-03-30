package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;

//tag::class[]
@DomainObject(mixinMethod = "property")                           // <.>
@Property()
@RequiredArgsConstructor
public class DomainObjectMixinMethod_initialCharacter {

    @SuppressWarnings("unused")
    private final DomainObjectMixinMethod mixee;

    @MemberSupport
    public Character property() {                                   // <.>
        val charArray = mixee.getName().toCharArray();
        return charArray.length == 0 ? null : charArray[0];
    }
}
//end::class[]
