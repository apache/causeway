package demoapp.dom.domain.objects.DomainObject.bounded.jpa;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingPage;

@Profile("demo-jpa")
//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectBoundingPage_find {

    @SuppressWarnings("unused")
    private final DomainObjectBoundingPage page;

    @MemberSupport
    public DomainObjectBoundingEntityImpl act(                // <.>
            final DomainObjectBoundingEntityImpl entity) {
        return entity;
    }

}
//end::class[]
