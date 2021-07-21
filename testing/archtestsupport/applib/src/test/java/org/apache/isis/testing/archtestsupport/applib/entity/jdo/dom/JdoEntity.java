package org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom;

import java.util.Comparator;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

@PersistenceCapable
@Uniques(@Unique(name = "name", members = {"name"}))
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class JdoEntity implements Comparable<JdoEntity> {

    private String name;

    @Override public int compareTo(final JdoEntity o) {
        return Comparator.<JdoEntity,String>comparing(x -> x.name).compare(this,o);
    }
}
