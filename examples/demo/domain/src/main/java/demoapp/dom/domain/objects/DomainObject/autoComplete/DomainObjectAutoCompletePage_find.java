package demoapp.dom.domain.objects.DomainObject.autoComplete;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectAutoCompletePage_find {

    @SuppressWarnings("unused")
    private final DomainObjectAutoCompletePage page;

    @MemberSupport
    public DomainObjectAutoCompleteEntity act(final DomainObjectAutoCompleteEntity domainObjectAutoComplete) {  // <.>
        return domainObjectAutoComplete;
    }

}
//end::class[]
