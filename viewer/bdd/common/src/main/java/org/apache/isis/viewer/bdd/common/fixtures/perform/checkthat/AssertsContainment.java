package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;



/**
 * Parameterizes {@link ThatSubcommand}s.
 */
public enum AssertsContainment {

    CONTAINS("contains", true, "(does not contain)"),
    DOES_NOT_CONTAIN(
            "does not contain", false, "(contains)");

    private final String key;
    private final boolean contains;
    private final String errorMsgIfNotSatisfied;

    private AssertsContainment(String key, boolean contains, String errorMsgIfNotSatisfied) {
        this.key = key;
        this.contains = contains;
        this.errorMsgIfNotSatisfied = errorMsgIfNotSatisfied;
    }

    public String getKey() {
        return key;
    }

    public boolean doesContain() {
        return contains;
    }

    public boolean isSatisfiedBy(final boolean contains) {
        return this.contains == contains;
    }

    public String getErrorMsgIfNotSatisfied() {
        return errorMsgIfNotSatisfied;
    }
}