package org.apache.isis.subdomains.base.applib.with;



public interface WithCodeComparable<T extends WithCodeComparable<T>> extends Comparable<T>, WithCodeGetter {
    void setCode(String code);
}