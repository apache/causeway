package org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom;

import java.util.Comparator;

import javax.inject.Inject;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@PersistenceCapable(schema = "jdo", identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE)
@Version
@Uniques(@Unique(name = "name", members = {"name"}))
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class JdoEntity implements Comparable<JdoEntity> {

    private String name;

    @Override public int compareTo(final JdoEntity o) {
        return Comparator.<JdoEntity,String>comparing(x -> x.name).compare(this,o);
    }

    @Inject @NotPersistent JdoService jdoService;
}
