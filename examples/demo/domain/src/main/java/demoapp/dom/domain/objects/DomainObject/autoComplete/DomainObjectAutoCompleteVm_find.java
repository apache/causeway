package demoapp.dom.domain.objects.DomainObject.autoComplete;

import demoapp.dom.domain.objects.DomainObject.aliased.DomainObjectAliased;
import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectAutoCompleteVm_find {

    @SuppressWarnings("unused")
    private final DomainObjectAutoCompleteVm mixee;

    @MemberSupport
    public DomainObjectAliased act(final DomainObjectAliased domainObjectAliased) {  // <.>
        return domainObjectAliased;
    }

}
//end::class[]
