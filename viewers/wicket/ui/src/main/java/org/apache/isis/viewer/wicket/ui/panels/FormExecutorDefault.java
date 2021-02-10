/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.wicket.ui.panels;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.FormExecutorContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.util.Components;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault<M extends FormExecutorContext>
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
     * @return <tt>false</tt> - if invalid args;
     * <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public boolean executeAndProcessResults(
            final Page page,
            final AjaxRequestTarget targetIfAny,
            final Form<?> feedbackFormIfAny,
            final boolean withinPrompt) {

        Command command = null;
        ManagedObject targetAdapter = null;

        final EntityModel targetEntityModel = model.getParentUiModel();

        try {

            // may immediately throw a concurrency exception if
            // the Isis Oid held in the underlying EntityModel is stale w.r.t. the DB.
            targetAdapter = obtainTargetAdapter();

            // no concurrency exception, so continue...

            // validate the proposed property value/action arguments
            final Optional<Recognition> invalidReasonIfAny = getReasonInvalidIfAny();
            if (invalidReasonIfAny.isPresent()) {
                raiseWarning(targetIfAny, feedbackFormIfAny, invalidReasonIfAny.get());
                return false;
            }

            val commonContext = targetEntityModel.getCommonContext();

            //
            // the following line will (attempt to) invoke the action, and will in turn either:
            //
            // 1. return a non-null result from a successful invocation
            //
            // 2. return a null result (from a successful action returning void)
            //
            // 3. throws a RuntimeException, either:
            //    a) as result of application throwing RecoverableException (DN transaction still intact)
            //    b) as result of DB exception, eg uniqueness constraint violation (DN transaction marked to abort)
            //
            // (The DB exception might actually be thrown by the flush() that follows.
            //
            val resultAdapter = obtainResultAdapter();
            // flush any queued changes; any concurrency or violation exceptions will actually be thrown here
            if(commonContext.getInteractionTracker().isInInteractionSession()) {
                commonContext.getTransactionService().flushTransaction();

                // update target, since version updated
                targetAdapter = targetEntityModel.getManagedObject();
                if(!EntityUtil.isDestroyed(targetAdapter)) {
                    targetEntityModel.resetPropertyModels();
                }
            }

            // hook to close prompt etc.
            onExecuteAndProcessResults(targetIfAny);

            final M model = this.model;
            RedirectFacet redirectFacet = null;
            if(model instanceof ActionModel) {
                final ActionModel actionModel = (ActionModel) model;
                redirectFacet = actionModel.getMetaModel().getFacet(RedirectFacet.class);
            }

            if (shouldRedirect(targetAdapter, resultAdapter, redirectFacet)
                    || hasBlobsOrClobs(page)
                    || targetIfAny == null) {

                redirectTo(resultAdapter, targetIfAny);

            } else {

                // in this branch the result must be same "logical" object as target, but
                // the OID might have changed if a view model.
                if (resultAdapter != null && targetAdapter != resultAdapter) {
                    targetEntityModel.setObject(resultAdapter);
                    targetAdapter = targetEntityModel.getManagedObject();
                }
                if(!EntityUtil.isDestroyed(targetAdapter)) {
                    targetEntityModel.resetPropertyModels();
                }

                // also in this branch we also know that there *is* an ajax target to use
                addComponentsToRedraw(targetIfAny);

                currentMessageBroker().ifPresent(messageBorker->{
                    final String jGrowlCalls = JGrowlUtil.asJGrowlCalls(messageBorker);
                    targetIfAny.appendJavaScript(jGrowlCalls);
                });

            }

            return true;

        } catch (Throwable ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

            // attempt to recognize this exception using the ExceptionRecognizers
            val messageWhenRecognized = recognizeException(ex, targetIfAny, feedbackFormIfAny)
                        .map(recog->recog.toMessage(getTranslationService()));

            // if we did recognize the message, and not inline prompt, then display to user as a growl pop-up
            if (messageWhenRecognized.isPresent() && !withinPrompt) {
                // ... display as growl pop-up

                currentMessageBroker().ifPresent(messageBroker->{
                    messageBroker.setApplicationError(messageWhenRecognized.get());
                });

                //TODO [2089] hotfix to render the error on the same page instead of redirecting;
                // previous behavior was to fall through and re-throw, which lead to the error never shown
                return false;
                //--
            }

            // irrespective, capture error in the Command, and propagate
            if (command != null) {

                //TODO (dead code) should happen at a more fundamental level
                // should not be a responsibility of the viewer

                command.updater().setResult(Result.failure(ex));

                //XXX legacy of
                //command.internal().setException(Throwables.getStackTraceAsString(ex));
            }

            // throwing an exception will get caught by WebRequestCycleForIsis#onException(...)
            // which will redirect to the error page.
            throw ex;
        }
    }

    private boolean shouldRedirect(
            final ManagedObject targetAdapter,
            final ManagedObject resultAdapter,
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

    private boolean differs(
            final ManagedObject targetAdapter,
            final ManagedObject resultAdapter) {

        final ObjectMemento targetOam = getCommonContext().mementoFor(targetAdapter);
        final ObjectMemento resultOam = getCommonContext().mementoFor(resultAdapter);

        return differs(targetOam, resultOam);
    }

    private static boolean differs(
            final ObjectMemento targetOam,
            final ObjectMemento resultOam) {

        val resultBookmark = resultOam != null ? resultOam.asHintingBookmarkIfSupported() : null;
        val targetBookmark = targetOam != null ? targetOam.asHintingBookmarkIfSupported() : null;

        return !Objects.equals(resultBookmark, targetBookmark);
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

//    private void forwardOnConcurrencyException(
//            final ManagedObject targetAdapter) {
//
//        // this will not preserve the URL (because pageParameters are not copied over)
//        // but trying to preserve them seems to cause the 302 redirect to be swallowed somehow
//        val entityPage = new EntityPage(model.getCommonContext() , targetAdapter);
//
//        // force any changes in state etc to happen now prior to the redirect;
//        // in the case of an object being returned, this should cause our page mementos
//        // (eg EntityModel) to hold the correct state.  I hope.
//        getCommonContext().getTransactionService().flushTransaction();
//
//        // "redirect-after-post"
//        val requestCycle = RequestCycle.get();
//        requestCycle.setResponsePage(entityPage);
//    }


    private static boolean shouldRedraw(final Component component) {

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

    private void addComponentsToRedraw(final AjaxRequestTarget target) {
        final Set<Component> componentsToRedraw = _Sets.newHashSet();
        final Set<Component> componentsNotToRedraw = _Sets.newHashSet();

        final Page page = target.getPage();
        page.visitChildren((component, visit) -> {
            if (!Components.isRenderedComponent(component)){
                return;
            }
            if(shouldRedraw(component)) {
                componentsToRedraw.add(component);
            } else {
                componentsNotToRedraw.add(component);
            }
        });

        for (Component component : componentsNotToRedraw) {

            component.visitParents(MarkupContainer.class, (parent, visit) -> {
                componentsToRedraw.remove(parent); // no-op if not in that list
            });

            if(component instanceof MarkupContainer) {
                val containerNotToRedraw = (MarkupContainer) component;
                containerNotToRedraw.visitChildren((child, visit) -> {
                        componentsToRedraw.remove(child); // no-op if not in that list
                });
            }
        }

        if(log.isDebugEnabled()) {
            debug(componentsToRedraw, componentsNotToRedraw);
        }

        for (Component component : componentsToRedraw) {
            Components.addToAjaxRequest(target, component);
        }
    }

    private void debug(
            final Collection<Component> componentsToRedraw,
            final Collection<Component> componentsNotToRedraw) {
        debug("Not redrawing", componentsNotToRedraw);
        debug("Redrawing", componentsToRedraw);
    }

    private void debug(
            final String title,
            final Collection<Component> list) {
        log.debug(">>> {}:", title);
        for (Component component : list) {
            log.debug(
                    String.format("%30s: %s",
                            component.getClass().getSimpleName(),
                            component.getPath()));

        }
    }

    private Optional<Recognition> recognizeException(
            final Throwable ex,
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        val recognition = getExceptionRecognizerService().recognize(ex);
        recognition.ifPresent(recog->raiseWarning(target, feedbackForm, recog));
        return recognition;
    }

    private void raiseWarning(
            final @Nullable AjaxRequestTarget targetIfAny,
            final @Nullable Form<?> feedbackFormIfAny,
            final @NonNull  Recognition recognition) {

        if(targetIfAny != null && feedbackFormIfAny != null) {
            //[ISIS-2419] for a consistent user experience with action dialog validation messages,
            //be less verbose (suppress the category) if its a Category.CONSTRAINT_VIOLATION.
            val errorMsg = recognition.getCategory()==Category.CONSTRAINT_VIOLATION
                    ? recognition.toMessageNoCategory(getTranslationService())
                    : recognition.toMessage(getTranslationService());
            targetIfAny.add(feedbackFormIfAny);
            feedbackFormIfAny.error(errorMsg);
        } else {
            val errorMsg = recognition.toMessage(getTranslationService());
            getMessageService().warnUser(errorMsg);
        }
    }

    // -- DEPENDENCIES

    private IsisAppCommonContext getCommonContext() {
        return model.getCommonContext();
    }

    protected ExceptionRecognizerService getExceptionRecognizerService() {
        return getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

    protected TranslationService getTranslationService() {
        return getServiceRegistry().lookupServiceElseFail(TranslationService.class);
    }

    protected MessageService getMessageService() {
        return getServiceRegistry().lookupServiceElseFail(MessageService.class);
    }

    protected ServiceRegistry getServiceRegistry() {
        return getCommonContext().getServiceRegistry();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getCommonContext().getSpecificationLoader();
    }

    protected InteractionFactory getIsisInteractionFactory() {
        return getCommonContext().lookupServiceElseFail(InteractionFactory.class);
    }

    protected Optional<MessageBroker> currentMessageBroker() {
        return getCommonContext().getInteractionTracker().currentMessageBroker();
    }

    protected WicketViewerSettings getSettings() {
        return getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);
    }

    ///////////////////////////////////////////////////////////////////////////////

    private ManagedObject obtainTargetAdapter() {
        return formExecutorStrategy.obtainTargetAdapter();
    }

    private Optional<Recognition> getReasonInvalidIfAny() {
        val reason = formExecutorStrategy.getReasonInvalidIfAny();
        val category = Category.CONSTRAINT_VIOLATION;
        return Recognition.of(category, reason);
    }

    private void onExecuteAndProcessResults(final AjaxRequestTarget target) {
        formExecutorStrategy.onExecuteAndProcessResults(target);
    }

    private ManagedObject obtainResultAdapter() {
        return formExecutorStrategy.obtainResultAdapter();
    }

    private void redirectTo(
            final ManagedObject resultAdapter,
            final AjaxRequestTarget target) {
        formExecutorStrategy.redirectTo(resultAdapter, target);
    }

}
