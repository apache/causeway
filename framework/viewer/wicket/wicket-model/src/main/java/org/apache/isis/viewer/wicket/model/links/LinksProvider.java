package org.apache.isis.viewer.wicket.model.links;

import java.util.List;

import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.wicket.markup.html.link.Link;

/**
 * For models - such as {@link EntityCollectionModel} - that can provide an
 * additional list of {@link Link}s to be rendered.
 */
public interface LinksProvider {
    List<LinkAndLabel> getLinks();
}
