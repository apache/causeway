package demoapp.dom.annotDomain.Action.command.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.Action.command.ActionCommandJdo;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "foregroundCommands"
)
@ActionLayout(
    named = "Clear"
)
public class ActionCommandJdo_clearForegroundCommands {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;
    public ActionCommandJdo_clearForegroundCommands(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }


    //tag::class[]
    public ActionCommandJdo act() {
        commandServiceSpiForActions.clearForegroundCommands();
        return actionCommandJdo;
    }

    @Inject
    private CommandServiceSpiForActions commandServiceSpiForActions;
}
//end::class[]
