package org.apache.isis.viewer.wicket.ui.util;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.jquery.IKey;

public class ExtendedPopoverConfig extends PopoverConfig {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Overflow constraint boundary of the popover. 
     * Accepts the values of 'viewport', 'window', 'scrollParent', or an HTMLElement reference (JavaScript only). 
     * For more information refer to Popper.js's preventOverflow docs.
     */
    private static final IKey<String> Boundary = newKey("boundary", PopoverBoundary.scrollParent.name());
    
    public enum PopoverBoundary {
    	viewport, window, scrollParent
    }
    
    public ExtendedPopoverConfig withBoundary(PopoverBoundary boundary) {
    	return withBoundary(boundary.name());
    }
    
    public ExtendedPopoverConfig withBoundary(String boundary) {
    	put(Boundary, boundary);
    	return this;
    }
}
