package org.apache.isis.core.runtime.services.inject;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DomainService(nature = NatureOfService.DOMAIN)
public class ServiceInjectorV1 implements ServiceInjector {

    @Inject IsisSessionFactory isisSessionFactory;

    @Override
    public <T> T injectServicesInto(@Nullable T domainObject) {
        isisSessionFactory.getServicesInjector().injectServicesInto(domainObject);
        return domainObject;
    }
}
