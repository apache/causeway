package org.apache.isis.applib.fixturescripts;

import org.apache.isis.applib.services.registry.ServiceRegistry2;

public interface PersonaWithFinder<T> {

    T findUsing(final ServiceRegistry2 serviceRegistry);

}

