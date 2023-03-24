package demoapp.dom.domain.objects.DomainObject.aliased;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

import lombok.RequiredArgsConstructor;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(associateWith = "people")
@RequiredArgsConstructor
public class DomainObjectAliasedVm_lookup {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedVm mixee;

    @MemberSupport
    public Customer act(final String bookmark) {
        return bookmarkService.lookup(Bookmark.parseElseFail(bookmark), Customer.class).orElseThrow(() -> new org.apache.causeway.applib.exceptions.RecoverableException("No customer exists for that bookmark"));
    }

    @Inject BookmarkService bookmarkService;

}
