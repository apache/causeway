package org.apache.isis.applib.bookmarks;

import org.apache.isis.applib.annotation.Programmatic;

public interface BookmarkHolder {

    @Programmatic
    Bookmark bookmark();
}
