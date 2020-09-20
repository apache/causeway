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
    , associateWith = "propertyAsync"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
)
@RequiredArgsConstructor
public class WrapperFactoryJdo_mixinUpdatePropertyAsync {

    @Inject WrapperFactory wrapperFactory;

    // ...
//end::class[]

    private final WrapperFactoryJdo wrapperFactoryJdo;

//tag::class[]
    public WrapperFactoryJdo act(final String value) {
        val control = AsyncControl.returningVoid().withSkipRules();
        val wrapped = this.wrapperFactory.asyncWrap(this.wrapperFactoryJdo, control);
        wrapped.setPropertyAsync(value);
        return this.wrapperFactoryJdo;
    }
    public String default0Act() {
        return wrapperFactoryJdo.getPropertyAsync();
    }
}
//end::class[]
