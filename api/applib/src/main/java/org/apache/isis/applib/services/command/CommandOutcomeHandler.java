package org.apache.isis.applib.services.command;

import java.sql.Timestamp;

import org.apache.isis.applib.services.bookmark.Bookmark;

public interface CommandOutcomeHandler {

    CommandOutcomeHandler NULL = new CommandOutcomeHandler() {
        @Override public Timestamp getStartedAt() { return null; }
        @Override public void setStartedAt(Timestamp startedAt) { }
        @Override public void setCompletedAt(Timestamp completedAt) { }
        @Override public void setResult(Bookmark resultBookmark) { }
        @Override public void setException(Throwable throwable) { }
    };

    Timestamp getStartedAt();
    void setStartedAt(Timestamp startedAt);

    void setCompletedAt(Timestamp completedAt);

    void setResult(Bookmark resultBookmark);
    void setException(Throwable throwable);
}
