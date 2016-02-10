package org.apache.isis.applib.layout.component;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.isis.applib.annotation.BookmarkPolicy;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasBookmarking {
    @XmlAttribute(required = false) BookmarkPolicy getBookmarking();

    void setBookmarking(BookmarkPolicy bookmarking);
}
