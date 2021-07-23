package org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom;

import java.util.Comparator;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

@PersistenceCapable(schema = "jdo")
@Unique(name = "name", members = {"name"})
@Version
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class JdoEntity2 implements Comparable<JdoEntity2> {

    private String name;

    @Override public int compareTo(final JdoEntity2 o) {
        return Comparator.<JdoEntity2,String>comparing(x -> x.name).compare(this,o);
    }
}

