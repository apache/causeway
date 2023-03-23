package demoapp.dom.domain.objects.DomainObject.aliased;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

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
