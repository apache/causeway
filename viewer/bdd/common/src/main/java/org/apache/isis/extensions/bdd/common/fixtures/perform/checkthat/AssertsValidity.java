package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.metamodel.consent.Consent;

/**
 * Parameterizes {@link ThatSubcommand}s.
 * 
 * @author Dan Haywood
 */
public enum AssertsValidity {

    VALID(true, "is valid for", "is valid", "valid"), INVALID(false,
            "is not valid for", "is not valid", "not valid", "invalid");

    private final String[] keys;
    private final boolean valid;

    AssertsValidity(boolean valid, String... keys) {
        this.keys = keys;
        this.valid = valid;
    }

    public String[] getKeys() {
        return keys;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean satisfiedBy(final Consent validityConsent) {
        return validityConsent.isAllowed() && isValid()
                || validityConsent.isVetoed() && !isValid();
    }

    /**
     * The reason to use if the assertion is not valid.
     */
    public String getReason(final Consent validityConsent) {
        return isValid() ? validityConsent.getReason() : "(valid)";
    }

    /**
     * The binding whose current cell should be coloured if the assertion fails.
     */
    public CellBinding colorBinding(final CellBinding arg0Binding, final CellBinding thatBinding) {
        if (arg0Binding == null) {
            return thatBinding;
        }
        return isValid() ? arg0Binding : thatBinding;
    }

}