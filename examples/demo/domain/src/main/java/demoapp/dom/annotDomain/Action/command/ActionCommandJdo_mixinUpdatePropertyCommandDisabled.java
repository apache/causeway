package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@Action(
    command = CommandReification.DISABLED
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyCommandDisabled"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(command = DISABLED)"
)
public class ActionCommandJdo_mixinUpdatePropertyCommandDisabled {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo_mixinUpdatePropertyCommandDisabled(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }

//tag::class[]
    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setProperty(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getProperty();
    }
}
//end::class[]
