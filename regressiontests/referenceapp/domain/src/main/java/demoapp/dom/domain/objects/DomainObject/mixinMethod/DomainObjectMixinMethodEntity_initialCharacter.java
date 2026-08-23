package demoapp.dom.domain.objects.DomainObject.mixinMethod;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;

import lombok.RequiredArgsConstructor;

//tag::class[]
@DomainObject(mixinMethod = "property")                         // <.>
@Property()
@RequiredArgsConstructor
public class DomainObjectMixinMethodEntity_initialCharacter {

    private final DomainObjectMixinMethodEntity mixee;

    @MemberSupport
    public Character property() {                               // <.>
        var charArray = mixee.getName().toCharArray();
        return charArray.length == 0 ? null : charArray[0];
    }
}
//end::class[]
