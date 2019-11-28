package org.apache.isis.extensions.base.dom.with;



public interface WithTitleComparable<T extends WithTitleComparable<T>> extends Comparable<T>, WithTitleGetter {
    void setTitle(String title);
}