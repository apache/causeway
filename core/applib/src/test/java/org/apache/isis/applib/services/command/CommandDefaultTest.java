package org.apache.isis.applib.services.command;

public class CommandDefaultTest extends Command2ContractTestAbstract {

    protected Command2 newCommand() {
        return new CommandDefault();
    }

}