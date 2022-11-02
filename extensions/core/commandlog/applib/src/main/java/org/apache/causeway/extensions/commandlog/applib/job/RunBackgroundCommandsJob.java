package org.apache.causeway.extensions.commandlog.applib.job;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.jaxb.JavaSqlJaxbAdapters;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.command.CommandOutcomeHandler;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

/**
 * An implementation of a Quartz {@link Job} that queries for {@link CommandLogEntry}s that have been persisted by
 * the {@link org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService} but not yet started; and then
 * executes them.
 *
 * @since 2.0 {@index}
 */
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class RunBackgroundCommandsJob implements Job {

    @Inject InteractionService interactionService;
    @Inject TransactionService transactionService;
    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;

    private final static JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter gregorianCalendarAdapter  = new JavaSqlJaxbAdapters.TimestampToXMLGregorianCalendarAdapter();;

    public void execute(final JobExecutionContext quartzContext) {
        val user = UserMemento.ofNameAndRoleNames("scheduler_user", "admin_role");
        val interactionContext = InteractionContext.builder().user(user).build();
        interactionService.run(interactionContext, new ExecuteNotYetStartedCommands());
    }

    private class ExecuteNotYetStartedCommands implements ThrowingRunnable {

        @Override
        public void run() {
            transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                val notYetStartedEntries = commandLogEntryRepository.findBackgroundAndNotYetStarted();
                for (val commandLogEntry : notYetStartedEntries) {
                    val commandDto = commandLogEntry.getCommandDto();
                    commandExecutorService.executeCommand(CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto, new OutcomeHandler(commandLogEntry));
                }
            }).ifFailureFail();

        }
    }

    @RequiredArgsConstructor
    private class OutcomeHandler implements CommandOutcomeHandler {

        private final CommandLogEntry commandLogEntry;

        @Override
        public Timestamp getStartedAt() {
            return commandLogEntry.getStartedAt();
        }

        @Override
        public void setStartedAt(Timestamp startedAt) {
            commandLogEntry.setStartedAt(startedAt);
        }

        @Override
        public void setCompletedAt(Timestamp completedAt) {
            commandLogEntry.setCompletedAt(completedAt);
        }

        @Override
        public void setResult(Try<Bookmark> resultBookmark) {
            resultBookmark.ifSuccess(bookmarkIfAny -> bookmarkIfAny.ifPresent(commandLogEntry::setResult));
        }
    }
}
