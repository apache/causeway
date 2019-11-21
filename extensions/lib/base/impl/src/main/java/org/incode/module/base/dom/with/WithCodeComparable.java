package org.incode.module.base.dom.with;



public interface WithCodeComparable<T extends WithCodeComparable<T>> extends Comparable<T>, WithCodeGetter {
    void setCode(String code);
}