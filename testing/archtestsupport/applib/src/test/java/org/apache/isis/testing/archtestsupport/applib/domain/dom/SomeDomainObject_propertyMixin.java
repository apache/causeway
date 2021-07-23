package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import org.apache.isis.applib.annotation.Property;

import lombok.RequiredArgsConstructor;

@Property
//@DomainObject(mixinMethod = "prop2")
@RequiredArgsConstructor
public class SomeDomainObject_propertyMixin {

    final SomeDomainObject someDomainObject;

    public void prop() {}

}
