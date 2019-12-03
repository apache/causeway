package org.apache.isis.metamodel.services.swagger.internal;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

public interface ClassExcluder {
    boolean exclude(final ObjectSpecification objectSpec);
    boolean exclude(ObjectAction objectAction);
}
