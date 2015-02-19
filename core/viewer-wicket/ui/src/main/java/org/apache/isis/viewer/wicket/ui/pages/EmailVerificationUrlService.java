package org.apache.isis.viewer.wicket.ui.pages;

import org.apache.isis.viewer.wicket.model.models.PageType;

/**
 * A Wicket specific service that may be used to create a link to a
 * page by {@link org.apache.isis.viewer.wicket.model.models.PageType page type}
 * with encoded/encrypted datum as first indexed parameter in the url for
 * mail verification purposes.
 */
public interface EmailVerificationUrlService {

    /**
     * Creates a url to the passed <em>pageClass</em> by encrypting the given
     * <em>datum</em> as a first indexed parameter
     *
     * @param pageType The type of the page to link to
     * @param datum The data to encrypt in the url
     * @return The full url to the page with the encrypted data
     */
    String createVerificationUrl(PageType pageType, String datum);
}
