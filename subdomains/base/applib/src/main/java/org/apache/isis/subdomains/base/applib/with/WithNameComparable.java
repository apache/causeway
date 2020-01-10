package org.apache.isis.subdomains.base.applib.with;



public interface WithNameComparable<T extends WithNameComparable<T>> extends Comparable<T>, WithNameGetter {
    
    void setName(String name);
}