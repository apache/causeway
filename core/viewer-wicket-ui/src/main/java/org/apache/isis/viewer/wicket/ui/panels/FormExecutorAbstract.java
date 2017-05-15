package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;

public abstract class FormExecutorAbstract<M extends BookmarkableModel<ObjectAdapter>> implements FormExecutor {

    protected final M model;

    public FormExecutorAbstract(final M model) {
        this.model = model;
    }

    @Override
    public boolean executeAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm,
            final PromptStyle promptStyle) {

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = obtainTargetAdapter();

            // no concurrency exception, so continue...
            return doExecuteAndProcessResults(target, feedbackForm, promptStyle);

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = obtainTargetAdapter();
            }

            forwardOnConcurrencyException(targetAdapter, ex);

            final MessageService messageService = getServicesInjector().lookupService(MessageService.class);
            messageService.warnUser(ex.getMessage());
            return false;
        }
    }

    /**
     * @param target
     * @return whether to clear args or not (they aren't if there was a validation exception)
     */
    private boolean doExecuteAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm,
            final PromptStyle promptStyle) {

        // validate the action parameters (if any)
        final String invalidReasonIfAny = getReasonInvalidIfAny();

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
            final ObjectAdapter resultAdapter = obtainResultAdapter();

            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();


            if(promptStyle == PromptStyle.INLINE) {

                final Page page = feedbackForm.getPage();
                addComponentsToRedraw(target, page);

            } else {
                forwardOntoResult(resultAdapter, target);

            }

            onExecuteAndProcessResults(target);

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
                            .handleResults(null, getIsisSessionFactory());
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

    private void addComponentsToRedraw(final AjaxRequestTarget target, final Page page) {
        final List<Component> componentsToRedraw = Lists.newArrayList();
        final List<Component> componentsNotToRedraw = Lists.newArrayList();

        page.visitChildren(new IVisitor<Component, Object>() {
            @Override
            public void component(final Component component, final IVisit<Object> visit) {
                if (component.getOutputMarkupId() && !(component instanceof Page)) {
                    List<Component> listToAddTo =
                            shouldRedraw(component)
                                    ? componentsToRedraw
                                    : componentsNotToRedraw;
                    listToAddTo.add(component);
                }
            }

            private boolean shouldRedraw(final Component component) {

                // hmm... this doesn't work, because I think that the components
                // get removed after they've been added to target.
                // so.. still getting WARN log messages from XmlPartialPageUpdate

//                final Page page = component.findParent(Page.class);
//                if(page == null) {
//                    // as per logic in XmlPartialPageUpdate, this has already been
//                    // removed from page so don't attempt to redraw it
//                    return false;
//                }

                final Object defaultModel = component.getDefaultModel();
                if (!(defaultModel instanceof ScalarModel)) {
                    return true;
                }
                final ScalarModel scalarModel = (ScalarModel) defaultModel;
                final UnchangingFacet unchangingFacet = scalarModel.getFacet(UnchangingFacet.class);
                return unchangingFacet == null || ! unchangingFacet.value() ;
            }
        });

        for (Component componentNotToRedraw : componentsNotToRedraw) {
            MarkupContainer parent = componentNotToRedraw.getParent();
            while(parent != null) {
                parent = parent.getParent();
            }

            componentNotToRedraw.visitParents(MarkupContainer.class, new IVisitor<MarkupContainer, Object>() {
                @Override
                public void component(final MarkupContainer parent, final IVisit<Object> visit) {
                    componentsToRedraw.remove(parent); // no-op if not in that list
                }
            });
            if(componentNotToRedraw instanceof MarkupContainer) {
                final MarkupContainer containerNotToRedraw = (MarkupContainer) componentNotToRedraw;
                containerNotToRedraw.visitChildren(new IVisitor<Component, Object>() {
                    @Override
                    public void component(final Component parent, final IVisit<Object> visit) {
                        componentsToRedraw.remove(parent); // no-op if not in that list
                    }
                });
            }
        }

        // debug(componentsToRedraw, componentsNotToRedraw);

        for (Component component : componentsToRedraw) {
            target.add(component);
        }
    }

    private void debug(
            final List<Component> componentsToRedraw,
            final List<Component> componentsNotToRedraw) {
        debug("Not redrawing", componentsNotToRedraw);
        debug("Redrawing", componentsToRedraw);
    }

    private void debug(final String title, final List<Component> list) {
        System.out.println(">>> " + title + ":");
        for (Component component : list) {
            System.out.println(
                    String.format("%30s: %s",
                            component.getClass().getSimpleName(),
                            component.getPath()));

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


    ///////////////////////////////////////////////////////////////////////////////

    protected abstract ObjectAdapter obtainTargetAdapter();

    protected abstract String getReasonInvalidIfAny();

    protected abstract void onExecuteAndProcessResults(final AjaxRequestTarget target);

    protected abstract ObjectAdapter obtainResultAdapter();

    protected abstract void forwardOnConcurrencyException(
            final ObjectAdapter targetAdapter,
            final ConcurrencyException ex);

    protected abstract void forwardOntoResult(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget target);


}
