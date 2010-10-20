package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;


public abstract class ThatValidityAbstract extends ThatSubcommandAbstract {

    private final AssertsValidity assertion;

    public ThatValidityAbstract(final AssertsValidity assertion) {
        super(assertion.getKeys());
        this.assertion = assertion;
    }

    public AssertsValidity getAssertion() {
        return assertion;
    }

}
