package demoapp.dom.domain.objects.DomainObject.bounded.jpa;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingPage;

//tag::class[]
@Profile("demo-jpa")
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectBoundingPage_find {

    @SuppressWarnings("unused")
    private final DomainObjectBoundingPage page;

    @MemberSupport
    public DomainObjectBoundingJpa act(final DomainObjectBoundingJpa domainObjectBounding) {  // <.>
        return domainObjectBounding;
    }

}
//end::class[]
