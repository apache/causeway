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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.util.Components;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault
implements FormExecutor {

    private static final long serialVersionUID = 1L;

    protected final WicketViewerSettings settings;
    private final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel;

    public FormExecutorDefault(
            final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        this.actionOrPropertyModel = actionOrPropertyModel;
        this.settings = getSettings();
    }

    /**
     * @return <tt>false</tt> - if invalid args;
     * <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public boolean executeAndProcessResults(
            final Page page,
            final AjaxRequestTarget ajaxTarget,
            final Form<?> feedbackFormIfAny,
            final boolean withinPrompt) {

        final EntityModel parentUiModel = actionOrPropertyModel.fold(
                act->act.getParentUiModel(),
                prop->prop.getParentUiModel());

        final ManagedObject owner = parentUiModel.getObject();
        final var commonContext = parentUiModel.getCommonContext();

        try {

            var targetAdapter = owner;

            // no concurrency exception, so continue...

            // validate the proposed property value/action arguments
            final Optional<Recognition> invalidReasonIfAny = getReasonInvalidIfAny();
            if (invalidReasonIfAny.isPresent()) {
                raiseWarning(ajaxTarget, feedbackFormIfAny, invalidReasonIfAny.get());
                return false; // invalid args, stay on page
            }

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
            val resultAdapter = actionOrPropertyModel.fold(
                    act->act.executeActionAndReturnResult(),
                    prop->prop.applyValueThenReturnOwner());

            val redirectFacet = actionOrPropertyModel.fold(
                    act->act.getMetaModel().getFacet(RedirectFacet.class),
                    prop->null);

            if(commonContext.getInteractionLayerTracker().isInInteraction()) {

                // flush any queued changes; any concurrency or violation exceptions will actually be thrown here
                commonContext.getTransactionService().flushTransaction();

                // TODO: REVIEW: I wonder why this next block is only performed within the outer if block?
                //  my guess is that the if block is always evaluated, in which case this is always run.

                if(willDefinitelyRedirect(redirectFacet)) {
                    // should circuit the redirect check later on; there's no need to reset the adapter
                    // (this also provides a workaround for view models wrapping now-deleted entities)
                    targetAdapter = ManagedObject.empty(targetAdapter.getSpecification());
                } else if(EntityUtil.isDetachedOrRemoved(targetAdapter)) {
                    // if this was an entity delete action
                    // then we don't re-fetch / re-create the targetAdapter
                    targetAdapter = ManagedObject.empty(targetAdapter.getSpecification());
                } else {
                    //View-models, when edited with AJAX requests, will change their state and will need
                    //to recreate their bookmark.
                    targetAdapter = ManagedObject.of(targetAdapter.getSpecification(), targetAdapter.getPojo());
                    parentUiModel.resetPropertyModels();
                }
            }

            if (ajaxTarget == null
                    || actionOrPropertyModel.fold(
                            act->act.getDirtiedAndClear(),
                            prop->prop.getDirtiedAndClear())
                    || shouldRedirect(targetAdapter, resultAdapter, redirectFacet)
                    || hasBlobsOrClobs(page)) {

                redirectTo(resultAdapter, ajaxTarget);

            } else {

                // in this branch the result must be same "logical" object as target, but
                // the OID might have changed if a view model.
                if (resultAdapter != null && targetAdapter != resultAdapter) {
                    parentUiModel.setObject(resultAdapter);
                    targetAdapter = parentUiModel.getManagedObject();
                }
                if(!EntityUtil.isDetachedOrRemoved(targetAdapter)) {
                    if(targetAdapter != null) {
                        getCommonContext().injectServicesInto(targetAdapter.getPojo());
                    }
                    parentUiModel.resetPropertyModels();
                }

                // also in this branch we also know that there *is* an ajax target to use
                addComponentsToRedraw(ajaxTarget);

                val configuration = getCommonContext().getConfiguration();
                currentMessageBroker().ifPresent(messageBorker->{
                    final String jGrowlCalls = JGrowlUtil.asJGrowlCalls(messageBorker, configuration);
                    ajaxTarget.appendJavaScript(jGrowlCalls);
                });

            }

            return true; // valid args, allow redirect

        } catch (Throwable ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

            // if inline prompt then redirect to error page
            if (withinPrompt) {
                // throwing an exception will get caught by WebRequestCycleForIsis#onException(...)
                throw ex; // redirect to the error page.
            }

            // attempt to recognize this exception using the ExceptionRecognizers
            if(recognizeExceptionThenRaise(ex, ajaxTarget, feedbackFormIfAny).isPresent()) {
                return false; // invalid args, stay on page
            }

            throw ex; // redirect to the error page.

        }
    }

    private boolean shouldRedirect(
            final ManagedObject targetAdapter,
            final ManagedObject resultAdapter,
            final RedirectFacet redirectFacet) {

        if(willDefinitelyRedirect(redirectFacet)) {
            return true;
        }

        return differs(targetAdapter, resultAdapter);
    }

    private boolean willDefinitelyRedirect(
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
            return false;
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
                final ScalarModel scalarModel = object.getModel();
                final UnchangingFacet unchangingFacet = scalarModel.getFacet(UnchangingFacet.class);
                return unchangingFacet != null && unchangingFacet.value();
            }

        });
        return hasBlobsOrClobs != null;
    }

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

    private Optional<Recognition> recognizeExceptionThenRaise(
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

        //[ISIS-2419] for a consistent user experience with action dialog validation messages,
        //be less verbose (suppress the category) if its a Category.CONSTRAINT_VIOLATION.
        val errorMsg = recognition.getCategory()==Category.CONSTRAINT_VIOLATION
                ? recognition.toMessageNoCategory(getTranslationService())
                : recognition.toMessage(getTranslationService());

        if(targetIfAny != null && feedbackFormIfAny != null) {
            targetIfAny.add(feedbackFormIfAny);
            feedbackFormIfAny.error(errorMsg);
        } else {
            getMessageService().warnUser(errorMsg);
        }
    }

    private Optional<Recognition> getReasonInvalidIfAny() {
        val reason = actionOrPropertyModel
                .fold(
                        act->act.getValidityConsent().getReason(),
                        prop->prop.getReasonInvalidIfAny());
        val category = Category.CONSTRAINT_VIOLATION;
        return Recognition.of(category, reason);
    }

    private void redirectTo(
            final ManagedObject resultAdapter,
            final AjaxRequestTarget ajaxTarget) {

        actionOrPropertyModel
        .accept(
                act->{
                    ActionResultResponse resultResponse = ActionResultResponseType
                        .determineAndInterpretResult(act, ajaxTarget, resultAdapter);
                    resultResponse
                        .getHandlingStrategy()
                        .handleResults(act.getCommonContext(), resultResponse);
                },
                prop->{
                    RequestCycle
                        .get()
                        .setResponsePage(new EntityPage(prop.getCommonContext(), resultAdapter));
                });
    }

    // -- DEPENDENCIES

    private IsisAppCommonContext getCommonContext() {
        return actionOrPropertyModel
                .fold(
                        act->act.getCommonContext(),
                        prop->prop.getCommonContext());
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

    protected InteractionService getIsisInteractionFactory() {
        return getCommonContext().lookupServiceElseFail(InteractionService.class);
    }

    protected Optional<MessageBroker> currentMessageBroker() {
        return getCommonContext().getMessageBroker();
    }

    protected WicketViewerSettings getSettings() {
        return getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);
    }

}
