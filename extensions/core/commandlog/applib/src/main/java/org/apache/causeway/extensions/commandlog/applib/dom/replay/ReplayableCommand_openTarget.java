package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.message.MessageService;

import org.jspecify.annotations.Nullable;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(
        associateWith = "target",
        describedAs = "Open recorded or actual target",
        position = ActionLayout.Position.PANEL
)
@RequiredArgsConstructor
public class ReplayableCommand_openTarget {

    private final ReplayableCommand replayableCommand;

    public enum TargetType {
        RECORDED,
        ACTUAL
    }

    @MemberSupport
    public Object act(TargetType targetType) {
        final var bookmarkStr =
                targetType == TargetType.RECORDED
                    ? replayableCommand.getTarget()
                    : replayableCommand.getActualTarget();
        return domainObject(targetType, bookmarkStr);
    }

    @MemberSupport
    public String disableAct() {
        return choicesTargetType().isEmpty() ? "No targets found." : null;
    }

    @MemberSupport
    public List<TargetType> choicesTargetType() {
        var list = new ArrayList<TargetType>();
        if (replayableCommand.getTarget() != null) {
            list.add(TargetType.RECORDED);
        }
        if (replayableCommand.getActualTarget() != null) {
            list.add(TargetType.ACTUAL);
        }
        return list;
    }

    @MemberSupport
    public TargetType defaultTargetType() {
        return choicesTargetType().stream().findFirst().orElse(TargetType.RECORDED);
    }

    private @Nullable Object domainObject(TargetType targetType, String bookmarkStr) {
        final Optional<Object> domainObjectIfAny = Optional.ofNullable(bookmarkStr).flatMap(Bookmark::parse).flatMap((Bookmark x) -> bookmarkService.lookup(x));
        if(domainObjectIfAny.isPresent()) {
            return domainObjectIfAny.get();
        }
        messageService.informUser(String.format("Unable to open %s target '%s'", targetType.name().toLowerCase(), bookmarkStr));
        return null;
    }

    @Inject BookmarkService bookmarkService;
    @Inject MessageService messageService;
}
