package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.DataFetchingEnvironment;
import lombok.Data;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;

import java.util.Optional;

@Data
public class GQLMeta {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;

    public String logicalTypeName(){
        return bookmark.getLogicalTypeName();
    }

    public String id(){
        return bookmark.getIdentifier();
    }

    public String version(){
        Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
        if (domainObject == null) return null;

        // TODO: implement; we would like to be this independent of the persistence mechanism
        return null;
    }

}
