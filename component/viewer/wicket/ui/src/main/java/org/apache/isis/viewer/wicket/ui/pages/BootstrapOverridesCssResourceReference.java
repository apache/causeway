package org.apache.isis.viewer.wicket.ui.pages;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * A CSS resource reference that provides CSS rules which override the CSS rules
 * provided by the currently active Bootstrap theme.
 * Usually the overrides rules are about sizes and weights, but should not change any colors
 */
public class BootstrapOverridesCssResourceReference extends CssResourceReference {

    public BootstrapOverridesCssResourceReference() {
        super(BootstrapOverridesCssResourceReference.class, "bootstrap-overrides.css");
    }
}
