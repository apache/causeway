package org.apache.isis.viewer.wicket.viewer.services;

import java.lang.annotation.Annotation;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;
import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceInjectorHolder;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * An implementation of {@link org.apache.isis.applib.services.guice.GuiceBeanProvider}
 * that uses the Injector configured for Wicket
 */
public class GuiceBeanProviderWicket implements GuiceBeanProvider {

    @Override
    public <T> T lookup(final Class<T> beanType) {
        Application application = Application.get();
        GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        Injector injector = injectorHolder.getInjector();
        return injector.getInstance(beanType);
    }

    @Override
    public <T> T lookup(final Class<T> beanType, final Annotation qualifier) {
        Application application = Application.get();
        GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        Injector injector = injectorHolder.getInjector();
        return injector.getInstance(Key.get(beanType, qualifier));
    }
}
