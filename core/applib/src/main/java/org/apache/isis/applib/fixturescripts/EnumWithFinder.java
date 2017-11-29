package org.apache.isis.applib.fixturescripts;

import org.apache.isis.applib.services.registry.ServiceRegistry2;

public interface EnumWithFinder<T> {

    T findUsing(final ServiceRegistry2 serviceRegistry);

}

