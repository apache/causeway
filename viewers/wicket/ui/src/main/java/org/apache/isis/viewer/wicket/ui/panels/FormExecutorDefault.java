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

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.FormExecutorContext;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault
implements FormExecutor {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static FormExecutor forAction(final ActionModel actionModel) {
        return new FormExecutorDefault(_Either.left(actionModel));
    }

    public static FormExecutor forProperty(final ScalarPropertyModel propertyModel) {
        return new FormExecutorDefault(_Either.right(propertyModel));
    }

    public static FormExecutor forMember(final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        return new FormExecutorDefault(actionOrPropertyModel);
    }

    // -- CONSTRUCTION

    protected final WicketViewerSettings settings;
    private final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel;

    private FormExecutorDefault(
            final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        this.actionOrPropertyModel = actionOrPropertyModel;
        this.settings = getSettings();
    }

    /**
     * @return <tt>false</tt> - if invalid args;
     * <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public FormExecutionOutcome executeAndProcessResults(
            final AjaxRequestTarget ajaxTarget,
            final Form<?> feedbackFormIfAny,
            final FormExecutorContext formExecutorContext) {

        try {

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("[EXECUTOR] start ...");
                //formExecutorContext.getParentObject().reloadViewmodelFromMemoizedBookmark();
            });

            final Optional<Recognition> invalidReasonIfAny = Recognition.of(
                    Category.CONSTRAINT_VIOLATION,
                    actionOrPropertyModel
                        .fold(
                                act->act.getValidityConsent().getReason(),
                                prop->prop.getReasonInvalidIfAny()));

            if (invalidReasonIfAny.isPresent()) {
                raiseWarning(ajaxTarget, feedbackFormIfAny, invalidReasonIfAny.get());
                return FormExecutionOutcome.FAILURE_RECOVERABLE_SO_STAY_ON_PAGE; // invalid args, stay on page
            }

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                final String whatIsExecuted = actionOrPropertyModel
                .fold(
                        act->act.getFriendlyName(),
                        prop->prop.getFriendlyName());

                _Debug.log("[EXECUTOR] execute %s ...", whatIsExecuted);
            });

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
            //XXX triggers BookmarkedObjectWkt.getObjectAndReAttach() down the call-stack
            //XXX applies the pending property
            val resultAdapter = actionOrPropertyModel.fold(
                    act->act.executeActionAndReturnResult(),
                    prop->prop.applyValueThenReturnOwner());

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{

                final String whatIsExecuted = actionOrPropertyModel
                .fold(
                        act->act.getFriendlyName(),
                        prop->prop.getFriendlyName());

                _Debug.log("[EXECUTOR] resultAdapter created for %s", whatIsExecuted);
            });

            // if we are in a nested dialog/form, that supports an action parameter,
            // the result must be fed back into the calling dialog's/form's parameter
            // negotiation model (instead of redirecting to a new page)
            if(formExecutorContext.getAssociatedParameter().isPresent()) {
                formExecutorContext.getAssociatedParameter().get()
                .setValue(resultAdapter);
                return FormExecutionOutcome.SUCCESS_IN_NESTED_CONTEXT_SO_STAY_ON_PAGE;
            }

            if(log.isDebugEnabled()) {
                log.debug("about to redirect with {} after execution result {}",
                        EntityUtil.getEntityState(resultAdapter),
                        resultAdapter);
            }

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("[EXECUTOR] interpret result ...");
            });

            //XXX triggers ManagedObject.getBookmarkRefreshed()
            val resultResponse = actionOrPropertyModel.fold(
                    act->ActionResultResponseType
                            .determineAndInterpretResult(act, ajaxTarget, resultAdapter, act.snapshotArgs()),
                    prop->ActionResultResponseType
                            .toEntityPage(resultAdapter));

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("[EXECUTOR] handle result ...");
            });

            // redirect using associated strategy
            resultResponse
                .getHandlingStrategy()
                .handleResults(getCommonContext(), resultResponse);

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("[EXECUTOR] ... return");
            });

            return FormExecutionOutcome.SUCCESS_AND_REDIRECED_TO_RESULT_PAGE; // success (valid args), allow redirect

        } catch (Throwable ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

            // attempt to recognize this exception using the ExceptionRecognizers (but only when not in inline prompt context!?)
            if(!formExecutorContext.isWithinInlinePrompt()
                    && recognizeExceptionThenRaise(ex, ajaxTarget, feedbackFormIfAny).isPresent()) {
                return FormExecutionOutcome.FAILURE_RECOVERABLE_SO_STAY_ON_PAGE; // invalid args, stay on page
            }

            // throwing an exception will get caught by WebRequestCycleForIsis#onException(...)
            throw ex; // redirect to the error page.
        }
    }

    // -- HELPER

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

    // -- DEPENDENCIES

    private IsisAppCommonContext getCommonContext() {
        return actionOrPropertyModel
                .fold(
                        act->act.getCommonContext(),
                        prop->prop.getCommonContext());
    }

    private ExceptionRecognizerService getExceptionRecognizerService() {
        return getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

    private TranslationService getTranslationService() {
        return getCommonContext().getTranslationService();
    }

    private MessageService getMessageService() {
        return getServiceRegistry().lookupServiceElseFail(MessageService.class);
    }

    private ServiceRegistry getServiceRegistry() {
        return getCommonContext().getServiceRegistry();
    }

    private WicketViewerSettings getSettings() {
        return getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);
    }



}
