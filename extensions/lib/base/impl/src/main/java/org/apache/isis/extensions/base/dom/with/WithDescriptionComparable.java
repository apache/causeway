package org.apache.isis.extensions.base.dom.with;



public interface WithDescriptionComparable<T extends WithDescriptionComparable<T>> 
        extends Comparable<T>, WithDescriptionGetter {
    void setDescription(String description);
}