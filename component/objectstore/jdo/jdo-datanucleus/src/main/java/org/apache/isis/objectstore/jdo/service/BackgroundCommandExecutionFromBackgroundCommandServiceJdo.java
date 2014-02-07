package org.apache.isis.objectstore.jdo.service;

import java.util.List;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.core.runtime.services.background.BackgroundCommandExecution;
import org.apache.isis.objectstore.jdo.applib.service.background.BackgroundCommandServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;

public final class BackgroundCommandExecutionFromBackgroundCommandServiceJdo extends BackgroundCommandExecution {

    @Override
    protected List<? extends Command> findBackgroundCommandsToExecute() {
        final List<CommandJdo> commands = backgroundCommandRepository.findBackgroundCommandsNotYetStarted();
        return commands; 
    }
    
    // //////////////////////////////////////

    @javax.inject.Inject
    private BackgroundCommandServiceJdoRepository backgroundCommandRepository;
}