package demoapp.dom.annotDomain._commands;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Collection
@CollectionLayout(defaultView = "table")
@RequiredArgsConstructor
public class ExposePersistedCommands_commands {
    // ...
//end::class[]
    private final ExposePersistedCommands exposePersistedCommands;

    //tag::class[]
    public List<CommandJdo> coll() {
        val list = new LinkedList<CommandJdo>();
        return commandJdoRepository.findCompleted();
    }

    @Inject CommandJdoRepository commandJdoRepository;
}
//end::class[]
