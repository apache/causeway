package org.apache.isis.core.metamodel.runtimecontext.spec.feature;

/**
 * powertype for subclasses.
 */
public enum FeatureType {
    PROPERTY,
    COLLECTION,
    ACTION,
    ACTION_PARAMETER;
    
    public boolean isProperty() {
        return this == PROPERTY;
    }
    public boolean isCollection() {
        return this == COLLECTION;
    }
    public boolean isAction() {
        return this == ACTION;
    }
    public boolean isActionParameter() {
        return this == ACTION_PARAMETER;
    }

    /**
     * Convenience.
     */
    public boolean isPropertyOrCollection() {
        return isProperty() || isCollection();
    }

}