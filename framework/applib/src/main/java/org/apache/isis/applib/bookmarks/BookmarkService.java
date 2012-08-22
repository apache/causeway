package org.apache.isis.applib.bookmarks;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;

@Named("Bookmarks")
public interface BookmarkService {

    @NotInServiceMenu
    Object lookup(BookmarkHolder bookmarkHolder);
    
}
