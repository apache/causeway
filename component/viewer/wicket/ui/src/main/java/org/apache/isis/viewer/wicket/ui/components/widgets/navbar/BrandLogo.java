package org.apache.isis.viewer.wicket.ui.components.widgets.navbar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandLogo extends WebComponent {

    private final String logoUrl;

    /**
     * Constructor.
     *
     * @param logoUrl The url to the brand logo image
     */
    public BrandLogo(final String logoUrl) {
        super("brandLogo");

        this.logoUrl = logoUrl;
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        tag.put("src", logoUrl);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(logoUrl != null);
    }
}
