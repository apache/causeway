package org.apache.isis.extensions.executionoutbox.jpa.integtests.model;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = "public",
        name = "Counter"
)
@Named("executionoutbox.test.Counter")
@DomainObject(nature = Nature.ENTITY)
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Counter extends org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter {

    @Id
    @GeneratedValue
    @Getter @Setter
    private Long id;

    @Column(nullable = false)
    @Getter @Setter
    private String name;

    @Column(nullable = true)
    @Getter @Setter
    private Long num;

    @Column(nullable = true)
    @Getter @Setter
    private Long num2;


}
