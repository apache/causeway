package org.apache.isis.viewer.wicket.ui.components.widgets.navbar;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandName extends Label {

    private final Placement placement;

    @Inject
    @Named("applicationName")
    private String applicationName;

    @com.google.inject.Inject(optional = true)
    @Named("brandLogoHeader")
    private String logoHeaderUrl;

    @com.google.inject.Inject(optional = true)
    @Named("brandLogoSignin")
    private String logoSigninUrl;

    /**
     * Constructor.
     *
     * @param id The component id
     * @param placement
     */
    public BrandName(final String id, final Placement placement) {
        super(id);
        this.placement = placement;
        setDefaultModel(Model.of(applicationName));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(placement.urlFor(logoHeaderUrl, logoSigninUrl) == null);
    }
}
