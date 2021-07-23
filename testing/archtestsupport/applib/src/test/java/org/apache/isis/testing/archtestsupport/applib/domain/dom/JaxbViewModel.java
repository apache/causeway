package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "JaxbViewModel")
public class JaxbViewModel {

    @Inject @XmlTransient SomeDomainService someDomainService;
}
