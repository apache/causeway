package org.apache.isis.extensions.commandlog.jdo.model;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PersistenceCapable(
        schema = "public",
        table = "Counter"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY)
@Named("commandlog.test.Counter")
@DomainObject(nature = Nature.ENTITY)
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Counter extends org.apache.isis.extensions.commandlog.applib.integtest.model.Counter {

    @PrimaryKey
    @Getter @Setter
    private Long id;

    @Column(allowsNull = "false")
    @Getter @Setter
    private String name;

    @Column(allowsNull = "true")
    @Getter @Setter
    private Long num;

    @Column(allowsNull = "true")
    @Getter @Setter
    private Long num2;


}
