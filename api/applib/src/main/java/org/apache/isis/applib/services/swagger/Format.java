package org.apache.isis.applib.services.swagger;

/**
 * The format to generate the representation of the swagger spec.
 *
 * @since 1.x {@index}
 */
public enum Format {
    /**
     * Generate a format in JSON (<code>text/json</code> media type).
     */
    JSON,
    /**
     * Generate a format in YAML (<code>application/yaml</code> media type).
     */
    YAML;

    /**
     * Returns the associated media type for each of the formats.
     *
     * <p>
     * Implementation note: not using subclasses of this enum, otherwise the
     * key in <code>translations.po</code> becomes more complex.
     * </p>
     */
    public String mediaType() {
        if (this == JSON) {
            return "text/json";
        } else {
            return "application/yaml";
        }
    }
}
