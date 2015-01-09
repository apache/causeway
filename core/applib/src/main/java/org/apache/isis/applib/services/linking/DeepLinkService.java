package org.apache.isis.applib.services.linking;

import java.net.URI;

/**
 * A service that creates a web link to any domain object in one of the Isis viewers.
 *
 * <p>
 *     The implementation is specific to the viewer(s) configured for Isis.  At the time of writing only one
 *     implementation is available, for the Wicket viewer.
 * </p>
 */
public interface DeepLinkService {

    /**
     * Creates a URI that can be used to obtain a representation of the provided domain object in one of the
     * Isis viewers.
     */
    URI deepLinkFor(Object domainObject);

}
