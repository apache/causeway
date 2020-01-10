package org.apache.isis.subdomains.base.applib.with;



public interface WithTitleComparable<T extends WithTitleComparable<T>> extends Comparable<T>, WithTitleGetter {
    void setTitle(String title);
}