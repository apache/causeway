package org.apache.isis.applib.services.exceprecog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorises each exception that has been recognised, as per
 * {@link Recognition#getCategory()}.
 * 
 * @since 1.x {@index}
 */
@RequiredArgsConstructor
public enum Category {
    /**
     * A violation of some declarative constraint (eg uniqueness or referential integrity) was detected.
     */
    CONSTRAINT_VIOLATION(
            "violation of some declarative constraint"),
    /**
     * The object to be acted upon cannot be found (404)
     */
    NOT_FOUND(
            "object not found"),
    /**
     * A concurrency exception, in other words some other user has changed this object.
     */
    CONCURRENCY(
            "concurrent modification"),
    /**
     * Recognized, but for some other reason... 40x error
     */
    CLIENT_ERROR(
            "client side error"),
    /**
     * 50x error
     */
    SERVER_ERROR(
            "server side error"),
    /**
     * Recognized, but uncategorized (typically: a recognizer of the original ExceptionRecognizer API).
     */
    OTHER(
            "other");

    @Getter
    private final String friendlyName;
}
