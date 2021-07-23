package org.apache.isis.testing.archtestsupport.applib.entity.jpa.dom;

import java.util.Comparator;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        schema = "jpa",
        uniqueConstraints = {@UniqueConstraint (name = "name", columnNames = "name")}
)
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ IsisEntityListener.class})
public class JpaEntity implements Comparable<JpaEntity> {

    @Id @Column(name = "id", nullable = false)
    private Long id;

    @Version
    private Long version;

    private String name;

    @Override public int compareTo(final JpaEntity o) {
        return Comparator.<JpaEntity,Long>comparing(x -> x.id).compare(this,o);
    }

    @Inject @Transient JpaService jpaService;
}
