package org.apache.causeway.viewer.graphql.viewer.test2.domain.dept;

import jakarta.inject.Named;
import jakarta.persistence.MappedSuperclass;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

@MappedSuperclass
@Named("university.dept.Person")
@DomainObject(nature = Nature.NOT_SPECIFIED)
public abstract class Person {

    @Property
    public abstract String getName();
}
