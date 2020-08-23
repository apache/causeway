package demoapp.dom.annotDomain.Action.command.spiimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandDefault;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.core.runtimeservices.background.BackgroundCommandExecution;
import org.apache.isis.core.security.authentication.standard.SimpleSession;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

@Service
@Order(OrderPrecedence.EARLY)       // <.>
public class CommandServiceSpiForActions implements CommandService {

    public static class CommandWithDtoDefault extends CommandDefault implements CommandWithDto {
        @Override
        public CommandDto asDto() {
            return CommandDtoUtils.fromXml(getMemento());
        }
    }

    private List<CommandWithDto> foregroundCommands = new ArrayList<>();
    private List<CommandWithDto> backgroundCommands = new ArrayList<>();

    @Override
    public Command create() {
        return new CommandWithDtoDefault();
    }

    @Override
    public boolean persistIfPossible(Command command) {
        return true;
    }

    @Override
    public void complete(Command command) {
        final CommandWithDtoDefault cwdd = (CommandWithDtoDefault) command;
        if(command.isPersistHint()) {
            final CommandExecuteIn executeIn = command.getExecuteIn();
            switch (executeIn) {
                case FOREGROUND:
                    foregroundCommands.add((CommandWithDto) command);
                case BACKGROUND:
                    backgroundCommands.add((CommandWithDto) command);
            }
        }
    }

    public Stream<CommandWithDto> streamForegroundCommands() {
        return foregroundCommands.stream();
    }

    public void clearForegroundCommands() {
        foregroundCommands.clear();
    }

    public Stream<CommandWithDto> streamBackgroundCommands() {
        return backgroundCommands.stream();
    }

    public void clearBackgroundCommands() {
        backgroundCommands.clear();
    }

    public void executeBackgroundCommands() {
        val execution = new BackgroundCommandExecution() {
            @Override
            protected List<? extends Command> findBackgroundCommandsToExecute() {
                return backgroundCommands;
            }
        };
        val simpleSession = new SimpleSession("sven", Collections.emptyList(), "");

        execution.execute(simpleSession, null);
        clearBackgroundCommands();
    }

}
