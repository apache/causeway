package org.apache.isis.extensions.base.dom.with;



public interface WithNameComparable<T extends WithNameComparable<T>> extends Comparable<T>, WithNameGetter {
    
    void setName(String name);
}