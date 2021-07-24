package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import org.apache.isis.applib.annotation.Action;

import lombok.RequiredArgsConstructor;

@Action
//@DomainObject(mixinMethod = "act2")
@RequiredArgsConstructor
public class SomeDomainObject_actionMixin {

    final SomeDomainObject someDomainObject;

    public void act(final String x) {}

}
