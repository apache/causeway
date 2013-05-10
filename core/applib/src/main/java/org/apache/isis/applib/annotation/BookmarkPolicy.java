package org.apache.isis.applib.annotation;

public enum BookmarkPolicy {
    /**
     * Can be bookmarked, and is a top-level 'root' (or parent) bookmark.
     */
    ROOT,
    /**
     * Can be bookmarked, but only as a child or some other parent/root bookmark
     */
    AS_CHILD,
    /**
     * An unimportant entity that should never be bookmarked.
     */
    NEVER
}