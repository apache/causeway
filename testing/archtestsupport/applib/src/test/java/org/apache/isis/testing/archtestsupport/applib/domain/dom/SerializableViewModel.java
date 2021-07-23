package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import java.io.Serializable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;

@DomainObject(nature = Nature.VIEW_MODEL, logicalTypeName = "SerializableViewModel")
@DomainObjectLayout()
public class SerializableViewModel implements Serializable {

    @Inject transient SomeDomainService someDomainService;
}
