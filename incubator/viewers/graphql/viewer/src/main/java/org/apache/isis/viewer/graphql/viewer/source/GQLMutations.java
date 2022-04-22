package org.apache.isis.viewer.graphql.viewer.source;

import java.util.List;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;

import lombok.Data;

@Data
public class GQLMutations {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;
    private final List<String> mutatorFieldNames;



}
