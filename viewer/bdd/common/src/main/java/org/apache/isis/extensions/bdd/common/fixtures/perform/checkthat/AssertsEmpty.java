package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;



/**
 * Parameterizes {@link ThatSubcommand}s.
 */
public enum AssertsEmpty {

    EMPTY("is empty", true, "(not empty)"), 
    NOT_EMPTY("is not empty", false,
            "(empty)");

    private final String key;
    private final boolean empty;
    private final String errorMsgIfNotSatisfied;

    AssertsEmpty(String key, boolean empty, String errorMsgIfNotSatisfied) {
        this.key = key;
        this.empty = empty;
        this.errorMsgIfNotSatisfied = errorMsgIfNotSatisfied;
    }

    public String getKey() {
        return key;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isSatisfiedBy(final boolean empty) {
        return this.empty == empty;
    }

    public String getErrorMsgIfNotSatisfied() {
        return errorMsgIfNotSatisfied;
    }
}