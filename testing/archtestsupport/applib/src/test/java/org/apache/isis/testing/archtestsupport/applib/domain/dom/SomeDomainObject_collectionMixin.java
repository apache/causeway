package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import org.apache.isis.applib.annotation.Collection;

import lombok.RequiredArgsConstructor;

@Collection
//@DomainObject(mixinMethod = "coll2")
@RequiredArgsConstructor
public class SomeDomainObject_collectionMixin {

    final SomeDomainObject someDomainObject;

    public void coll() {}

}
