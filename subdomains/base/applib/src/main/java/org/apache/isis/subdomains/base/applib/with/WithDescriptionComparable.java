package org.apache.isis.subdomains.base.applib.with;



public interface WithDescriptionComparable<T extends WithDescriptionComparable<T>> 
        extends Comparable<T>, WithDescriptionGetter {
    void setDescription(String description);
}