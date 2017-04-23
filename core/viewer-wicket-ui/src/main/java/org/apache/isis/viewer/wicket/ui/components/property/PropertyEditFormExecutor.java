package org.apache.isis.viewer.wicket.ui.components.property;

import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class PropertyEditFormExecutor implements FormExecutor {

    private MarkupContainer panel;
    private final ScalarModel scalarModel;

    public PropertyEditFormExecutor(final MarkupContainer panel, final ScalarModel scalarModel) {
        this.panel = panel;
        this.scalarModel = scalarModel;
    }

    @Override
    public boolean executeAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        Components.permanentlyHide(panel, ComponentType.ENTITY_ICON_AND_TITLE);

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = scalarModel.getParentObjectAdapterMemento().getObjectAdapter(
                                AdapterManager.ConcurrencyChecking.CHECK,
                                getPersistenceSession(), getSpecificationLoader());

            // no concurrency exception, so continue...
            return editTargetAndProcessResults(target, feedbackForm);

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = scalarModel.getParentObjectAdapterMemento().getObjectAdapter(
                                    AdapterManager.ConcurrencyChecking.CHECK,
                        getPersistenceSession(), getSpecificationLoader());
            }

            // page redirect/handling
            final EntityPage entityPage = new EntityPage(targetAdapter, null);
            panel.setResponsePage(entityPage);

            getMessageBroker().addWarning(ex.getMessage());
            return false;
        }

    }

    private boolean editTargetAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        // validate the action parameters (if any)
        final String invalidReasonIfAny = this.scalarModel.getReasonInvalidIfAny();

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

            ObjectAdapter adapter = this.scalarModel.getParentObjectAdapterMemento().getObjectAdapter(
                                        AdapterManager.ConcurrencyChecking.CHECK,
                                        getPersistenceSession(), getSpecificationLoader());

            final ObjectAdapter objectAdapter = this.scalarModel.applyValue(adapter);
            // (borrowed some code previously in EntityModel)
            if (objectAdapter != adapter) {
                this.scalarModel.getParentObjectAdapterMemento().setAdapter(adapter);
            }

            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();

            // disabling concurrency checking after the layout XML (grid) feature
            // was throwing an exception when rebuild grid after invoking edit prompt.
            // not certain why that would be the case, but (following similar code for action prompt)
            // think it should be safe to simply disable while recreating the page to re-render back to user.
            final EntityPage entityPage =
                    AdapterManager.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                            new Callable<EntityPage>() {
                                @Override public EntityPage call() throws Exception {
                                    return new EntityPage(objectAdapter, null);
                                }
                            }
                    );

            panel.setResponsePage(entityPage);

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

    private void raiseWarning(AjaxRequestTarget target, Form<?> feedbackForm, String error) {
        if(target != null && feedbackForm != null) {
            target.add(feedbackForm);
            feedbackForm.error(error);
        } else {
            getMessageBroker().addWarning(error);
        }
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

    protected IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }


}
