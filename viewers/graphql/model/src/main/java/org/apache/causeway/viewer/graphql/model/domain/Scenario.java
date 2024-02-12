package org.apache.causeway.viewer.graphql.model.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.bookmark.Bookmark;

import org.apache.causeway.applib.services.bookmark.BookmarkService;

import org.springframework.stereotype.Service;

/**
 * Holds the state of an executing scenario.
 *
 * <p>
 *     Note that although this is a singleton, it is <i>not</i> thread-safe.
 * </p>
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class Scenario {

    private final BookmarkService bookmarkService;

    @Getter @Setter private String name;

    private final Map<String, Bookmark> references = new HashMap<>();

    @Programmatic
    public void putReference(String reference, final Object pojo) {
        Bookmark bookmark = bookmarkService.bookmarkFor(pojo).orElseThrow();
        references.put(reference, bookmark);
    }

    @Programmatic
    public Object getReference(String reference) {
        return bookmarkService.lookup(references.get(reference)).orElseThrow();
    }

    @Programmatic
    public void clearReferences() {
        references.clear();
    }
}
