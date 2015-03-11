package org.apache.isis.viewer.wicket.viewer.services;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;

/**
 * An implementation of {@link org.apache.isis.applib.services.guice.GuiceBeanProvider}
 * that uses the Injector configured for Wicket
 */
public class GuiceBeanProviderWicket implements GuiceBeanProvider {

    @Override
    public <T> T lookup(Class<T> cls) {
        Application application = Application.get();
        GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        Injector injector = injectorHolder.getInjector();
        Binding<T> binding = injector.getBinding(cls);
        Provider<T> provider = binding.getProvider();
        return provider.get();
    }
}
