package org.apache.isis.core.metamodel.runtimecontext.spec.feature;

/**
 * powertype for subclasses.
 */
public enum MemberType {
    PROPERTY,
    COLLECTION,
    ACTION;
    public boolean isAction() {
        return this == ACTION;
    }
    public boolean isAssociation() {
        return !isAction();
    }
    public boolean isCollection() {
        return this == COLLECTION;
    }
    public boolean isProperty() {
        return this == PROPERTY;
    }
}