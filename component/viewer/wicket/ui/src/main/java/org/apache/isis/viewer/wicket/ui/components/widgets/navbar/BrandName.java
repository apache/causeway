package org.apache.isis.viewer.wicket.ui.components.widgets.navbar;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandName extends Label {

    @Inject
    @Named("applicationName")
    private String applicationName;


    @com.google.inject.Inject(optional = true)
    @Named("brandLogo")
    private String logoUrl;

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public BrandName(final String id) {
        super(id);
        setDefaultModel(Model.of(applicationName));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(logoUrl == null);
    }
}
