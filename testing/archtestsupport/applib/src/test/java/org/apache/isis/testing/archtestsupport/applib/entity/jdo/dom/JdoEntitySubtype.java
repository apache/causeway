package org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom;

import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

@PersistenceCapable(schema = "jdo")
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class JdoEntitySubtype extends JdoEntity2<JdoEntitySubtype> {

    public JdoEntitySubtype(final String name) {
        super(name);
    }

}

