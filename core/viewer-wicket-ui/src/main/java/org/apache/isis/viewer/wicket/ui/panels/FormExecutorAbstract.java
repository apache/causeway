package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ParentEntityModelProvider;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.IsisBlobOrClobPanelAbstract;

public abstract class FormExecutorAbstract<M extends BookmarkableModel<ObjectAdapter> & ParentEntityModelProvider>
        implements FormExecutor {

    protected final M model;
    protected final WicketViewerSettings settings;

    public FormExecutorAbstract(final M model) {
        this.model = model;
        this.settings = getSettings();
    }

    protected WicketViewerSettings getSettings() {
        final GuiceBeanProvider guiceBeanProvider = getServicesInjector().lookupService(GuiceBeanProvider.class);
        return guiceBeanProvider.lookup(WicketViewerSettings.class);
    }

    @Override
    public boolean executeAndProcessResults(
            final Page page,
            final AjaxRequestTarget targetIfAny,
            final Form<?> feedbackFormIfAny) {

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = obtainTargetAdapter();

            // no concurrency exception, so continue...
            return doExecuteAndProcessResults(page, targetIfAny, feedbackFormIfAny);

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
     *
     * @param page
     * @param targetIfAny
     * @return whether to clear args or not (they aren't if there was a validation exception)
     */
    private boolean doExecuteAndProcessResults(
            final Page page,
            final AjaxRequestTarget targetIfAny,
            final Form<?> feedbackFormIfAny) {

        // validate the action parameters (if any)
        final String invalidReasonIfAny = getReasonInvalidIfAny();

        if (invalidReasonIfAny != null) {
            raiseWarning(targetIfAny, feedbackFormIfAny, invalidReasonIfAny);
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
            getPersistenceSession().getPersistenceManager().flush();

            // update target, since version updated (concurrency checks)
            final EntityModel targetEntityModel = model.getParentEntityModel();

            targetEntityModel.resetVersion();
            targetEntityModel.resetPropertyModels();

            onExecuteAndProcessResults(targetIfAny);

            final ObjectAdapterMemento resultOam = ObjectAdapterMemento.createOrNull(resultAdapter);
            final ObjectAdapterMemento targetOam = targetEntityModel.getObjectAdapterMemento();
            if (shouldForward(page, resultOam, targetOam) || targetIfAny == null) {

                forwardOntoResult(resultAdapter, targetIfAny);

            } else {
                final AjaxRequestTarget target = targetIfAny; // only in this branch if there *is* a target to use
                addComponentsToRedraw(target);
            }

            return true;

        } catch (RuntimeException ex) {

            String message = recognizeException(ex, targetIfAny, feedbackFormIfAny);

            if (message != null) {
                // no need to add to message broker, should already have been added...

                if (feedbackFormIfAny == null) {
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

    private boolean shouldForward(
            final Page page,
            final ObjectAdapterMemento resultOam,
            final ObjectAdapterMemento targetOam) {

        final Bookmark resultBookmark = resultOam.asHintingBookmark();
        final Bookmark targetBookmark = targetOam.asHintingBookmark();

        if (shouldForward(resultBookmark, targetBookmark)) {
            return true;
        }

        // this is a bit of a hack... currently the blob/clob panel doesn't correctly redraw itself.
        // we therefore force a reforward (unless is declared as unchanging).
        final Object hasBlobsOrClobs = page.visitChildren(IsisBlobOrClobPanelAbstract.class,
                new IVisitor<IsisBlobOrClobPanelAbstract, Object>() {
                    @Override
                    public void component(final IsisBlobOrClobPanelAbstract object, final IVisit<Object> visit) {
                        if (!isUnchanging(object)) {
                            visit.stop(true);
                        }
                    }

                    private boolean isUnchanging(final IsisBlobOrClobPanelAbstract object) {
                        final ScalarModel scalarModel = (ScalarModel) object.getModel();
                        final UnchangingFacet unchangingFacet = scalarModel.getFacet(UnchangingFacet.class);
                        return unchangingFacet != null && unchangingFacet.value();
                    }

                });
        return hasBlobsOrClobs != null;
    }

    private boolean shouldForward(final Bookmark resultBookmark, final Bookmark targetBookmark) {
        final boolean redirectEvenIfSameObject = getSettings().isRedirectEvenIfSameObject();

        if(resultBookmark == null && targetBookmark == null) {
            return redirectEvenIfSameObject;
        }
        if (resultBookmark == null || targetBookmark == null) {
            return true;
        }
        final String resultBookmarkStr = asStr(resultBookmark);
        final String targetBookmarkStr = asStr(targetBookmark);

        return !Objects.equals(resultBookmarkStr, targetBookmarkStr)  || redirectEvenIfSameObject;
    }

    private static String asStr(final Bookmark bookmark) {
        return bookmark instanceof HintStore.BookmarkWithHintId
                ? ((HintStore.BookmarkWithHintId) bookmark).toStringUsingHintId()
                : bookmark.toString();
    }

    private void addComponentsToRedraw(final AjaxRequestTarget target) {
        final List<Component> componentsToRedraw = Lists.newArrayList();
        final List<Component> componentsNotToRedraw = Lists.newArrayList();

        final Page page = target.getPage();
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

        debug(componentsToRedraw, componentsNotToRedraw);

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

    private void raiseWarning(
            final AjaxRequestTarget targetIfAny,
            final Form<?> feedbackFormIfAny,
            final String error) {

        if(targetIfAny != null && feedbackFormIfAny != null) {
            targetIfAny.add(feedbackFormIfAny);
            feedbackFormIfAny.error(error);
        } else {
            final MessageService messageService = getServicesInjector().lookupService(MessageService.class);
            messageService.warnUser(error);
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
