package org.apache.isis.applib.services.hint;

/**
 * Provides an SPI for view models to implement to represent
 * their "logical" identity (stable even if the view model's state changes).
 *
 * <p>
 *     Hints are stored against the `Bookmark` of a domain object, essentially
 *     the identifier of the domain object. For a domain entity this identifier
 *     is fixed and unchanging but for view models the identifier changes each
 *     time the view model's state changes (the identifier is basically a
 *     digest of the object's state).
 *     This means that any hints stored against the view model's bookmark are
 *     in effect lost as soon as the view model is modified.
 * </p>
 *
 * <p>
 *     This SPI therefore allows a view model to take advantage of the hinting
 *     mechanism of the viewer by providing a "logical" identity stored which
 *     hints for the view model can be stored.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface HintIdProvider {
    String hintId();
}
