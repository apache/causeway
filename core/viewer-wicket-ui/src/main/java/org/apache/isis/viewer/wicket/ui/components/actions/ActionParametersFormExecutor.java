package org.apache.isis.viewer.wicket.ui.components.actions;

import java.util.List;

import com.google.common.base.Throwables;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class ActionParametersFormExecutor implements FormExecutor {

    private final MarkupContainer panel;
    private final ActionModel actionModel;

    public ActionParametersFormExecutor(final MarkupContainer panel, final ActionModel actionModel) {
        this.panel = panel;
        this.actionModel = actionModel;
    }

    private ActionPrompt actionPrompt;

    /**
     * @param feedbackForm - for feedback messages.
     * @return
     */
    @Override
    public boolean executeAndProcessResults(AjaxRequestTarget target, Form<?> feedbackForm) {

        Components.permanentlyHide(panel, ComponentType.ENTITY_ICON_AND_TITLE);

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = actionModel.getTargetAdapter();

            // no concurrency exception, so continue...
            return executeActionOnTargetAndProcessResults(target, feedbackForm);

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = actionModel.getTargetAdapter();
            }

            // forward onto the target page with the concurrency exception
            ActionResultResponse resultResponse = ActionResultResponseType.OBJECT
                    .interpretResult(actionModel, targetAdapter, ex);
            resultResponse.getHandlingStrategy().handleResults(panel, resultResponse, getIsisSessionFactory());

            getMessageBroker().addWarning(ex.getMessage());
            return false;
        }
    }

    /**
     * @param target
     * @return whether to clear args or not (they aren't if there was a validation exception)
     */
    private boolean executeActionOnTargetAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        // validate the action parameters (if any)
        final String invalidReasonIfAny = actionModel.getReasonInvalidIfAny();

        if (invalidReasonIfAny != null) {
            raiseWarning(target, feedbackForm, invalidReasonIfAny);
            return false;
        }

        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command;
        if (commandContext != null) {
            command = commandContext.getCommand();
            command.setExecutor(Command.Executor.USER);
        } else {
            command = null;
        }

        // the object store could raise an exception (eg uniqueness constraint)
        // so we handle it here.
        try {
            // could be programmatic flushing, so must include in the try... finally
            final ObjectAdapter resultAdapter = actionModel.executeHandlingApplicationExceptions();

            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();

            ActionResultResponse resultResponse = ActionResultResponseType
                    .determineAndInterpretResult(actionModel, target, resultAdapter);
            resultResponse.getHandlingStrategy().handleResults(panel, resultResponse, getIsisSessionFactory());

            if (actionModel.isBookmarkable()) {
                bookmarkPage(actionModel);
            }

            if (actionPrompt != null) {
                actionPrompt.closePrompt(target);
                // cos will be reused next time, so mustn't cache em.
                actionModel.clearArguments();
            }

            return true;

        } catch (RuntimeException ex) {

            String message = recognizeException(ex, target, feedbackForm);

            if (message != null) {
                // no need to add to message broker, should already have been added...

                if (feedbackForm == null) {
                    // forward on instead to void page
                    // (otherwise, we'll have rendered an action parameters page
                    // and so we'll be staying on that page)
                    ActionResultResponseHandlingStrategy.REDIRECT_TO_VOID
                            .handleResults(panel, null, getIsisSessionFactory());
                }

                return false;
            }

            // not handled, so capture and propagate
            if (command != null) {
                command.setException(Throwables.getStackTraceAsString(ex));
            }

            throw ex;
        }
    }

    private String recognizeException(RuntimeException ex, AjaxRequestTarget target, Form<?> feedbackForm) {

        // REVIEW: this code is similar to stuff in EntityPropertiesForm, perhaps move up to superclass?
        // REVIEW: similar code also in WebRequestCycleForIsis; combine?

        // see if the exception is recognized as being a non-serious error
        // (nb: similar code in WebRequestCycleForIsis, as a fallback)
        List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector()
                .lookupServices(ExceptionRecognizer.class);
        String recognizedErrorIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        if (recognizedErrorIfAny != null) {

            // recognized
            raiseWarning(target, feedbackForm, recognizedErrorIfAny);

            getTransactionManager().getCurrentTransaction().clearAbortCause();

            // there's no need to abort the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).
        }
        return recognizedErrorIfAny;
    }

    public void raiseWarning(AjaxRequestTarget target, Form<?> feedbackForm, String error) {
        if (target != null && feedbackForm != null) {
            target.add(feedbackForm);
            feedbackForm.error(error);
        } else {
            getMessageBroker().addWarning(error);
        }
    }

    void setActionPrompt(final ActionPrompt actionPrompt) {
        this.actionPrompt = actionPrompt;
    }

    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    protected ServicesInjector getServicesInjector() {
        return getIsisSessionFactory().getServicesInjector();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) getSession();
        return application.getBookmarkedPagesModel();
    }

    Session getSession() {
        return Session.get();
    }

}
