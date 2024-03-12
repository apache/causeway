package org.apache.causeway.applib.services.ascii;

/**
 * Optional SPI to provide an equivalent identifier using only ASCII character set, for the specified feature
 * (a property, collection, action or action parameter).
 *
 * <p>
 *     The GraphQL API (viewer) may require this SPI to be implemented if any of the features use non-ASCII characters,
 *     because feature Ids are used as GraphQL (field) names.
 * </p>
 *
 * @see <a href="https://spec.graphql.org/October2021/#sec-Names">GraphQL specification</a>.
 *
 * @since 2.x {@index}
 */
public interface AsciiIdentifierService {

    String asciiIdFor(String featureId);

}
