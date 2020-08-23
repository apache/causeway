package demoapp.dom.annotDomain.Action.command.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.Action.command.ActionCommandJdo;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "backgroundCommands"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Execute"
)
public class ActionCommandJdo_executeBackgroundCommands {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;
    public ActionCommandJdo_executeBackgroundCommands(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }


    //tag::class[]
    public ActionCommandJdo act() {
        commandServiceSpiForActions.clearBackgroundCommands();
        return actionCommandJdo;
    }

    @Inject
    private CommandServiceSpiForActions commandServiceSpiForActions;
}
//end::class[]
