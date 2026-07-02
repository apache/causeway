package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.message.MessageService;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(
        associateWith = "target",
        position = ActionLayout.Position.PANEL
)
@RequiredArgsConstructor
public class ReplayableCommand_openTarget {

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Object act() {
        final var bookmarkIfAny = replayableCommand.targetBookmarkIfAny().flatMap(Bookmark::parse);
        if (bookmarkIfAny.isPresent()) {
            final var targetIfAny = bookmarkService.lookup(bookmarkIfAny.get());
            if (targetIfAny.isPresent()) {
                return targetIfAny.get();
            }
        }
        messageService.informUser(String.format("Unable to open target '%s'", replayableCommand.targetBookmarkIfAny().orElse(null)));
        return null;
    }

    @MemberSupport
    public String disableAct() {
        return replayableCommand.targetBookmarkIfAny().isPresent() ? null : "No target bookmark";
    }

    @Inject BookmarkService bookmarkService;
    @Inject MessageService messageService;
}
