package org.apache.isis.viewer.wicket.model.links;

import org.apache.wicket.markup.html.link.AbstractLink;

public class LinkAndLabel {
    private final AbstractLink link;
    private final String label;

    public LinkAndLabel(final AbstractLink link, final String label) {
        this.link = link;
        this.label = label;
    }

    public AbstractLink getLink() {
        return link;
    }

    public String getLabel() {
        return label;
    }
}