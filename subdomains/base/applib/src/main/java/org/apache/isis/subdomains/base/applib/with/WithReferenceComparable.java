package org.apache.isis.subdomains.base.applib.with;



public interface WithReferenceComparable<T extends WithReferenceComparable<T>> 
        extends Comparable<T>, WithReferenceGetter {
    void setReference(String reference);
}