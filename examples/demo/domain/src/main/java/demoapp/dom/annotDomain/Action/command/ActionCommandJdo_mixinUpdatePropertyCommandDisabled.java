package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(
    command = CommandReification.DISABLED       // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyCommandDisabled"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(command = DISABLED)"
)
@RequiredArgsConstructor
public class ActionCommandJdo_mixinUpdatePropertyCommandDisabled {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setPropertyCommandDisabled(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getPropertyCommandDisabled();
    }
//tag::class[]
}
//end::class[]
