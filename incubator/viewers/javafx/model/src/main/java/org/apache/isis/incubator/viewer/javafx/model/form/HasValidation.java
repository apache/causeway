package org.apache.isis.incubator.viewer.javafx.model.form;

import java.io.Serializable;

/**
 * A component that supports input validation.
 *
 */
public interface HasValidation extends Serializable {

    /**
     * Sets an error message to the component.
     * <p>
     * The Web Component is responsible for deciding when to show the error
     * message to the user, and this is usually triggered by triggering the
     * invalid state for the Web Component. Which means that there is no need to
     * clean up the message when component becomes valid (otherwise it may lead
     * to undesired visual effects).
     *
     * @param errorMessage
     *            a new error message
     */
    void setErrorMessage(String errorMessage);

    /**
     * Gets current error message from the component.
     *
     * @return current error message
     */
    String getErrorMessage();

    /**
     * Sets the validity of the component input.
     * <p>
     * When component becomes valid it hides the error message by itself, so
     * there is no need to clean up the error message via the
     * {@link #setErrorMessage(String)} call.
     *
     * @param invalid
     *            new value for component input validity
     */
    void setInvalid(boolean invalid);

    /**
     * Returns {@code true} if component input is invalid, {@code false}
     * otherwise.
     *
     * @return whether the component input is valid
     */
    boolean isInvalid();
}
