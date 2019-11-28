package org.apache.isis.extensions.base.dom.with;



public interface WithCodeComparable<T extends WithCodeComparable<T>> extends Comparable<T>, WithCodeGetter {
    void setCode(String code);
}