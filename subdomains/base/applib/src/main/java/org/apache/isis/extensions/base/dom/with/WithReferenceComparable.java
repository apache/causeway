package org.apache.isis.extensions.base.dom.with;



public interface WithReferenceComparable<T extends WithReferenceComparable<T>> 
        extends Comparable<T>, WithReferenceGetter {
    void setReference(String reference);
}