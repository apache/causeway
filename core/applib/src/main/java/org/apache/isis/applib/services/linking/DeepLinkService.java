package org.apache.isis.applib.services.linking;

import java.net.URI;

/**
 * A service that creates a link to any POJO
 */
public interface DeepLinkService {

    /**
     * @param pojo The POJO to link to
     * @return The link to the POJO
     */
    URI deepLinkFor(Object pojo);
}
