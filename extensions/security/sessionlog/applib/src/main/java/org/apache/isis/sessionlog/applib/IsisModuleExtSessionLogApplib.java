package org.apache.isis.sessionlog.applib;

import org.eclipse.persistence.logging.SessionLog;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.sessionlog.applib.app.SessionLogMenu;
import org.apache.isis.sessionlog.applib.spiimpl.SessionLogServiceDefault;
import org.apache.isis.sessionlog.applib.spiimpl.SessionLogServiceInitializer;


@Configuration
@Import({
        SessionLogMenu.class,
        SessionLogServiceInitializer.class,
        SessionLogServiceDefault.class
})
public class IsisModuleExtSessionLogApplib {

    public static final String NAMESPACE = "isis.ext.sessionlog";
    public static final String SCHEMA = "isisExtSessionLog";

    public abstract static class TitleUiEvent<S>
            extends org.apache.isis.applib.events.ui.TitleUiEvent<S> { }

    public abstract static class IconUiEvent<S>
            extends org.apache.isis.applib.events.ui.IconUiEvent<S> { }

    public abstract static class CssClassUiEvent<S>
            extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> { }

    public abstract static class LayoutUiEvent<S>
            extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> { }


    public abstract static class ActionDomainEvent<S>
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {}

    public abstract static class CollectionDomainEvent<S, T>
    extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S, T> {}

    public abstract static class PropertyDomainEvent<S, T>
    extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S, T> {}

}
