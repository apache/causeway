package org.apache.isis.applib.services.jaxb;

import org.apache.isis.applib.mixins.dto.Dto_downloadXsd;

/**
 * Controls whether, when generating {@link JaxbService#toXsd(Object, IsisSchemas) XML schemas},
 * any of the common Isis schemas (in the namespace <code>http://org.apache.isis.schema</code>) should be included
 * or just ignored (and therefore don't appear in the returned map).
 *
 * <p>
 * The practical benefit of this is that for many DTOs there will only be one other
 * schema, that of the DTO itself.  The {@link Dto_downloadXsd} mixin uses this to return that single XSD,
 * rather than generating a ZIP of two schemas (the Isis schema and the one for the DTO), as it would otherwise;
 * far more convenient when debugging and so on.  The Isis schemas can always be
 * <a href="http://isis.apache.org/schema">downloaded</a> from the Isis website.
 * </p>
 */
public enum IsisSchemas {
    INCLUDE,
    IGNORE;

    /**
     * Implementation note: not using subclasses, otherwise the key in translations.po becomes more complex.
     */
    public boolean shouldIgnore(final String namespaceUri) {
        if (this == INCLUDE) {
            return false;
        } else {
            return namespaceUri.matches(".*isis\\.apache\\.org.*");
        }
    }
}
