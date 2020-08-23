package demoapp.dom.annotDomain.Action.command.spiimpl;

import java.util.LinkedList;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

import demoapp.dom.annotDomain.Action.command.ActionCommandJdo;

//tag::class[]
@Collection
public class ActionCommandJdo_backgroundCommands {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;
    public ActionCommandJdo_backgroundCommands(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }

    //tag::class[]
    public LinkedList<CommandDto> coll() {
        val list = new LinkedList<CommandDto>();
        commandServiceSpiForActions
                .streamBackgroundCommands()
                .map(CommandWithDto::asDto)
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    private CommandServiceSpiForActions commandServiceSpiForActions;
}
//end::class[]
