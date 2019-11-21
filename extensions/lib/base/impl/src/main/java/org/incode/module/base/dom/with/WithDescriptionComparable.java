package org.incode.module.base.dom.with;



public interface WithDescriptionComparable<T extends WithDescriptionComparable<T>> 
        extends Comparable<T>, WithDescriptionGetter {
    void setDescription(String description);
}