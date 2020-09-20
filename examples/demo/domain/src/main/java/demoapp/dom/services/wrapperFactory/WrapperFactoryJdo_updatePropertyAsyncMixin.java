package demoapp.dom.services.wrapperFactory;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyAsyncMixin"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Update Property Async"
    , describedAs = "Mixin that Updates 'property async mixin' directly"
)
@RequiredArgsConstructor
public class WrapperFactoryJdo_updatePropertyAsyncMixin {
    // ...
//end::class[]

    private final WrapperFactoryJdo wrapperFactoryJdo;

//tag::class[]
    public WrapperFactoryJdo act(final String value) {
        wrapperFactoryJdo.setPropertyAsyncMixin(value);
        return wrapperFactoryJdo;
    }
    public String default0Act() {
        return wrapperFactoryJdo.getPropertyAsyncMixin();
    }
}
//end::class[]
