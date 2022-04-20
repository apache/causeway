package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "demo",
        name = "E1"
)
@DomainObject(nature = Nature.ENTITY, logicalTypeName = "gqltestdomain.E1")
public class E1 implements TestEntity, Comparable {

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "e2_id")
    private E2 e2;

    @Override
    public int compareTo(Object o) {
        E1 e1 = (E1) o;
        return this.getName().compareTo(e1.getName());
    }
}
