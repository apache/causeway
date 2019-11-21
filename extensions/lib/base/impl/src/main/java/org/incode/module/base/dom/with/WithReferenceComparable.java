package org.incode.module.base.dom.with;



public interface WithReferenceComparable<T extends WithReferenceComparable<T>> 
        extends Comparable<T>, WithReferenceGetter {
    void setReference(String reference);
}