package demoapp.dom.domain.objects.DomainObject.bounded.jpa;

import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingVm;
import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.springframework.context.annotation.Profile;

//tag::class[]
@Profile("demo-jpa")
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectBoundingVm_find {

    @SuppressWarnings("unused")
    private final DomainObjectBoundingVm mixee;

    @MemberSupport
    public DomainObjectBoundingJpa act(final DomainObjectBoundingJpa domainObjectBounding) {  // <.>
        return domainObjectBounding;
    }

}
//end::class[]
