/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisRequestCycle;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ParentEntityModelProvider;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.base.Throwables;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault<M extends BookmarkableModel<ObjectAdapter> & ParentEntityModelProvider>
implements FormExecutor {

    private static final long serialVersionUID = 1L;

    protected final M model;
    protected final WicketViewerSettings settings;
    private final FormExecutorStrategy<M> formExecutorStrategy;

    public FormExecutorDefault(final FormExecutorStrategy<M> formExecutorStrategy) {
        this.model = formExecutorStrategy.getModel();
        this.settings = getSettings();
        this.formExecutorStrategy = formExecutorStrategy;
    }

    /**
     *
     * @param page
     * @param targetIfAny
     * @param feedbackFormIfAny
     * @param withinPrompt
     *
     * @return <tt>false</tt> - if invalid args; if concurrency exception; <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public boolean executeAndProcessResults(
            final Page page,
            final AjaxRequestTarget targetIfAny,
            final Form<?> feedbackFormIfAny,
            final boolean withinPrompt) {

        Command command = null;
        ObjectAdapter targetAdapter = null;

        final EntityModel targetEntityModel = model.getParentEntityModel();

        try {

            // may immediately throw a concurrency exception if
            // the Isis Oid held in the underlying EntityModel is stale w.r.t. the DB.
            targetAdapter = obtainTargetAdapter();

            // no concurrency exception, so continue...

            // validate the proposed property value/action arguments
            final String invalidReasonIfAny = getReasonInvalidIfAny();
            if (invalidReasonIfAny != null) {
                raiseWarning(targetIfAny, feedbackFormIfAny, invalidReasonIfAny);
                return false;
            }

            val commandContext = currentCommandContext().orElse(null);
            if (commandContext != null) {
                command = commandContext.getCommand();
                command.internal().setExecutor(Command.Executor.USER);
            }


            //
            // the following line will (attempt to) invoke the action, and will in turn either:
            //
            // 1. return a non-null result from a successful invocation
            //
            // 2. return a null result (from a successful action returning void)
            //
            // 3. throws a RuntimeException, either:
            //    a) as result of application throwing RecoverableException/ApplicationException (DN xactn still intact)
            //    b) as result of DB exception, eg uniqueness constraint violation (DN xactn marked to abort)
            //    Either way, as a side-effect the Isis transaction will be set to MUST_ABORT (IsisTransactionManager does this)
            //
            // (The DB exception might actually be thrown by the flush() that follows.
            //
            final ObjectAdapter resultAdapter = obtainResultAdapter();
            // flush any queued changes; any concurrency or violation exceptions will actually be thrown here
            IsisRequestCycle.onResultAdapterObtained();


            // update target, since version updated (concurrency checks)
            targetEntityModel.resetVersion();
            targetAdapter = targetEntityModel.load();
            if(!targetAdapter.isDestroyed()) {
                targetEntityModel.resetPropertyModels();
            }


            // hook to close prompt etc.
            onExecuteAndProcessResults(targetIfAny);

            final M model = this.model;
            RedirectFacet redirectFacet = null;
            if(model instanceof ActionModel) {
                final ActionModel actionModel = (ActionModel) model;
                redirectFacet = actionModel.getActionMemento().getAction(getSpecificationLoader()).getFacet(RedirectFacet.class);
            }

            if (shouldRedirect(targetAdapter, resultAdapter, redirectFacet) ||
                hasBlobsOrClobs(page)                                       ||
                targetIfAny == null                                             ) {

                redirectTo(resultAdapter, targetIfAny);

            } else {

                // in this branch the result must be same "logical" object as target, but
                // the OID might have changed if a view model.
                if (resultAdapter != null && targetAdapter != resultAdapter) {
                    targetEntityModel.setObject(resultAdapter);
                    targetAdapter = targetEntityModel.load();
                }
                if(!targetAdapter.isDestroyed()) {
                    targetEntityModel.resetPropertyModels();
                }

                // also in this branch we also know that there *is* an ajax target to use
                addComponentsToRedraw(targetIfAny);

                final String jGrowlCalls = JGrowlUtil.asJGrowlCalls(getAuthenticationSession().getMessageBroker());
                targetIfAny.appendJavaScript(jGrowlCalls);
            }

            return true;

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = obtainTargetAdapter();
            }

            forwardOnConcurrencyException(targetAdapter, ex);

            getMessageService().warnUser(ex.getMessage());

            return false;

        } catch (RuntimeException ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).


            // see if is an application-defined exception. If so, convert to an application error,
            final RecoverableException appEx = RecoverableException.Util.getRecoverableExceptionIfAny(ex);
            String message = null;
            if (appEx != null) {
                message = appEx.getMessage();
            }

            // otherwise, attempt to recognize this exception using the ExceptionRecognizers
            if(message == null) {
                message = recognizeException(ex, targetIfAny, feedbackFormIfAny);
            }

            // if we did recognize the message, and not inline prompt, then display to user as a growl pop-up
            if (message != null && !withinPrompt) {
                // ... display as growl pop-up
                final MessageBroker messageBroker = getAuthenticationSession().getMessageBroker();
                messageBroker.setApplicationError(message);
                
                //TODO [2089] hotfix to render the error on the same page instead of redirecting;
                // previous behavior was to fall through and rethrow, which lead to the error never shown
                return false;
                //--
            }

            // irrespective, capture error in the Command, and propagate
            if (command != null) {
                command.internal().setException(Throwables.getStackTraceAsString(ex));
            }

            // throwing an exception will get caught by WebRequestCycleForIsis#onException(...)
            // which will redirect to the error page.
            throw ex;
        }
    }

    private boolean shouldRedirect(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter resultAdapter,
            final RedirectFacet redirectFacet) {

        if(redirectFacet == null) {
            return getSettings().isRedirectEvenIfSameObject();
        }

        switch (redirectFacet.policy()) {

        case EVEN_IF_SAME:
        default:
            return true;

        case AS_CONFIGURED:
            final boolean redirectEvenIfSameObject = getSettings().isRedirectEvenIfSameObject();
            if (redirectEvenIfSameObject) {
                return true;
            }
            // fall through to...

        case ONLY_IF_DIFFERS:
            return differs(targetAdapter, resultAdapter);
        }
    }

    private static boolean differs(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter resultAdapter) {

        final ObjectAdapterMemento targetOam = ObjectAdapterMemento.ofAdapter(targetAdapter);
        final ObjectAdapterMemento resultOam = ObjectAdapterMemento.ofAdapter(resultAdapter);

        return differs(targetOam, resultOam);
    }

    private static boolean differs(
            final ObjectAdapterMemento targetOam,
            final ObjectAdapterMemento resultOam) {

        final Bookmark resultBookmark = resultOam != null ? resultOam.asHintingBookmarkIfSupported() : null;
        final Bookmark targetBookmark = targetOam != null ? targetOam.asHintingBookmarkIfSupported() : null;

        return differs(targetBookmark, resultBookmark);
    }

    private static boolean differs(
            final Bookmark targetBookmark,
            final Bookmark resultBookmark) {

        if(resultBookmark == null && targetBookmark == null) {
            return true;
        }
        if (resultBookmark == null || targetBookmark == null) {
            return true;
        }
        final String resultBookmarkStr = asStr(resultBookmark);
        final String targetBookmarkStr = asStr(targetBookmark);

        return !Objects.equals(resultBookmarkStr, targetBookmarkStr);
    }

    private boolean hasBlobsOrClobs(final Page page) {

        // this is a bit of a hack... currently the blob/clob panel doesn't correctly redraw itself.
        // we therefore force a re-forward (unless is declared as unchanging).
        final Object hasBlobsOrClobs = page.visitChildren(IsisBlobOrClobPanelAbstract.class,
                new IVisitor<IsisBlobOrClobPanelAbstract<?>, Object>() {
            @Override
            public void component(final IsisBlobOrClobPanelAbstract<?> object, final IVisit<Object> visit) {
                if (!isUnchanging(object)) {
                    visit.stop(true);
                }
            }

            private boolean isUnchanging(final IsisBlobOrClobPanelAbstract<?> object) {
                final ScalarModel scalarModel = (ScalarModel) object.getModel();
                final UnchangingFacet unchangingFacet = scalarModel.getFacet(UnchangingFacet.class);
                return unchangingFacet != null && unchangingFacet.value();
            }

        });
        return hasBlobsOrClobs != null;
    }

    private static String asStr(final Bookmark bookmark) {
        return bookmark instanceof HintStore.BookmarkWithHintId
                ? ((HintStore.BookmarkWithHintId) bookmark).toStringUsingHintId()
                        : bookmark.toString();
    }

    private void forwardOnConcurrencyException(
            final ObjectAdapter targetAdapter,
            final ConcurrencyException ex) {

        // this will not preserve the URL (because pageParameters are not copied over)
        // but trying to preserve them seems to cause the 302 redirect to be swallowed somehow
        final EntityPage entityPage =

                // disabling concurrency checking after the layout XML (grid) feature
                // was throwing an exception when rebuild grid after invoking action
                // not certain why that would be the case, but think it should be
                // safe to simply disable while recreating the page to re-render back to user.
                ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                        new Callable<EntityPage>() {
                            @Override public EntityPage call() throws Exception {
                                return new EntityPage(targetAdapter, ex);
                            }
                        }
                        );

        // force any changes in state etc to happen now prior to the redirect;
        // in the case of an object being returned, this should cause our page mementos
        // (eg EntityModel) to hold the correct state.  I hope.
        val txManager = IsisContext.getTransactionManagerJdo().get();
        txManager.flushTransaction();

        // "redirect-after-post"
        val requestCycle = RequestCycle.get();
        requestCycle.setResponsePage(entityPage);
    }


    private void addComponentsToRedraw(final AjaxRequestTarget target) {
        final List<Component> componentsToRedraw = _Lists.newArrayList();
        final List<Component> componentsNotToRedraw = _Lists.newArrayList();

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

        if(log.isDebugEnabled()) {
            debug(componentsToRedraw, componentsNotToRedraw);
        }

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
        log.debug(">>> {}:", title);
        for (Component component : list) {
            log.debug(
                    String.format("%30s: %s",
                            component.getClass().getSimpleName(),
                            component.getPath()));

        }
    }

    private String recognizeException(RuntimeException ex, AjaxRequestTarget target, Form<?> feedbackForm) {

        // REVIEW: similar code also in WebRequestCycleForIsis; combine?

        // see if the exception is recognized as being a non-serious error
        // (nb: similar code in WebRequestCycleForIsis, as a fallback)
        final Stream<ExceptionRecognizer> exceptionRecognizers = getServiceRegistry()
                .select(ExceptionRecognizer.class)
                .stream();
        String recognizedErrorIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        if (recognizedErrorIfAny != null) {

            // recognized
            raiseWarning(target, feedbackForm, recognizedErrorIfAny);

            val txManager = IsisContext.getTransactionManagerJdo().get();
            txManager.getCurrentTransaction().clearAbortCause();

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
            final MessageService messageService = getServiceRegistry().lookupServiceElseFail(MessageService.class);
            messageService.warnUser(error);
        }
    }


    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////


    protected IsisSession getCurrentSession() {
        return IsisSession.currentOrElseNull();
    }

    protected ServiceRegistry getServiceRegistry() {
        return IsisContext.getServiceRegistry();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getCurrentSession().getAuthenticationSession();
    }

    private MessageService getMessageService() {
    	return getServiceRegistry().lookupServiceElseFail(MessageService.class);
    }
    
    protected WicketViewerSettings getSettings() {
    	return getServiceRegistry().lookupServiceElseFail(WicketViewerSettings.class);
    }

    // request-scoped
    private Optional<CommandContext> currentCommandContext() {
    	return getServiceRegistry().lookupService(CommandContext.class);
    }
    

    ///////////////////////////////////////////////////////////////////////////////

    private ObjectAdapter obtainTargetAdapter() {
        return formExecutorStrategy.obtainTargetAdapter();
    }

    private String getReasonInvalidIfAny() {
        return formExecutorStrategy.getReasonInvalidIfAny();
    }

    private void onExecuteAndProcessResults(final AjaxRequestTarget target) {
        formExecutorStrategy.onExecuteAndProcessResults(target);
    }

    private ObjectAdapter obtainResultAdapter() {
        return formExecutorStrategy.obtainResultAdapter();
    }

    private void redirectTo(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget target) {
        formExecutorStrategy.redirectTo(resultAdapter, target);
    }

}
