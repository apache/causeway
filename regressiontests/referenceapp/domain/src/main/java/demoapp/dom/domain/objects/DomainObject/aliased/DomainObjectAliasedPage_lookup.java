package demoapp.dom.domain.objects.DomainObject.aliased;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectAliasedPage_lookup {

    @SuppressWarnings("unused")
    private final DomainObjectAliasedPage page;

    @MemberSupport
    public DomainObjectAliasedEntity act(final String bookmark) {
        return bookmarkService.lookup(
                Bookmark.parseElseFail(bookmark),
                DomainObjectAliasedEntity.class
        ).orElseThrow(() -> new RecoverableException("No customer exists for that bookmark"));
    }
    public List<String> choices0Act() {
        var bookmarks = new ArrayList<String>();
        var aliases = repository.all();
        aliases.stream().forEach(obj -> {
            bookmarks.add(obj.getBookmark());           // <.>
            bookmarks.add(obj.getPreviousBookmark());   // <.>
        });
        return bookmarks;
    }

    @Inject DomainObjectAliasedEntityRepository repository;
    @Inject BookmarkService bookmarkService;
}
//end::class[]
