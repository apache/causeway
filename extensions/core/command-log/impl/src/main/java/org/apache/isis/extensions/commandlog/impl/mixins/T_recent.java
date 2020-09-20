package org.apache.isis.extensions.commandlog.impl.mixins;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

@Collection(
    domainEvent = T_recent.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
public abstract class T_recent<T> {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<T_recent, CommandJdo> { }

    private final T domainObject;
    public T_recent(final T domainObject) {
        this.domainObject = domainObject;
    }

    public List<CommandJdo> coll() {
        return findRecent();
    }

    private List<CommandJdo> findRecent() {
        final Bookmark bookmark = bookmarkService.bookmarkFor(domainObject);
        return queryResultsCache.execute(
                () -> commandJdoRepository.findRecentByTarget(bookmark)
                , T_recent.class
                , "findRecentByTarget"
                , domainObject);
    }

    @Inject
    CommandJdoRepository commandJdoRepository;
    @Inject BookmarkService bookmarkService;
    @Inject QueryResultsCache queryResultsCache;

}
