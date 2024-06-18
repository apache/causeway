package org.apache.causeway.applib.exceptions.unrecoverable;

import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.i18n.TranslatableString;

/**
 * Indicates that a bookmark cannot be found.
 *
 * @since 2.x {@index}
 *
 */
public class BookmarkNotFoundException extends UnrecoverableException {
    public BookmarkNotFoundException(String msg) {
        super(msg);
    }

    public BookmarkNotFoundException(TranslatableString translatableMessage, Class<?> translationContextClass, String translationContextMethod) {
        super(translatableMessage, translationContextClass, translationContextMethod);
    }

    public BookmarkNotFoundException(Throwable cause) {
        super(cause);
    }

    public BookmarkNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BookmarkNotFoundException(TranslatableString translatableMessage, Class<?> translationContextClass, String translationContextMethod, Throwable cause) {
        super(translatableMessage, translationContextClass, translationContextMethod, cause);
    }
}