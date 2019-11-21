package org.incode.module.base.dom.with;



public interface WithTitleComparable<T extends WithTitleComparable<T>> extends Comparable<T>, WithTitleGetter {
    void setTitle(String title);
}