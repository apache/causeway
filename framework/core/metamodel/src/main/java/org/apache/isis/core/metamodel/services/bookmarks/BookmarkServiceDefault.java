package org.apache.isis.core.metamodel.services.bookmarks;

import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.bookmarks.Bookmark;
import org.apache.isis.applib.bookmarks.BookmarkHolder;
import org.apache.isis.applib.bookmarks.BookmarkService;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.DomainObjectServicesAware;

public class BookmarkServiceDefault implements BookmarkService, DomainObjectServicesAware {

    private DomainObjectServices domainObjectServices;
    
    @Override
    @NotInServiceMenu
    public Object lookup(BookmarkHolder bookmarkHolder) {
        Bookmark bookmark = bookmarkHolder.bookmark();
        Object lookup = domainObjectServices.lookup(bookmark);
        return lookup;
    }

    @Override
    @Programmatic
    public void setDomainObjectServices(DomainObjectServices domainObjectServices) {
        this.domainObjectServices = domainObjectServices;
    }

}
