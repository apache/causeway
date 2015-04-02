package org.apache.isis.viewer.wicket.viewer.services;

import java.lang.annotation.Annotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;

/**
 * An implementation of {@link org.apache.isis.applib.services.guice.GuiceBeanProvider}
 * that uses the Injector configured for Wicket
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GuiceBeanProviderWicket implements GuiceBeanProvider {

    @Programmatic
    @Override
    public <T> T lookup(final Class<T> beanType) {
        final Application application = Application.get();
        final GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        final Injector injector = injectorHolder.getInjector();
        return injector.getInstance(beanType);
    }

    @Programmatic
    @Override
    public <T> T lookup(final Class<T> beanType, final Annotation qualifier) {
        final Application application = Application.get();
        final GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        final Injector injector = injectorHolder.getInjector();
        return injector.getInstance(Key.get(beanType, qualifier));
    }
}
